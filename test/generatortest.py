import png
import random
import math
import opensimplex

2500.0

width = 256 # 128: 7s; 256: 48s; 512: 381s; 1024x512: 2151s : seems to scale at 2c
height = width
seed = random.randint(0, 3e12)
# seed = 2880678423922 
bw = False
sigma = 3 # island to pangean tendency- higher numbers mean more pangean [1-5], 3 default
density = 6 # higher density = more final nodes = more water [1-10], 6 default
wetness = 0.004
oceanSize = 0.55 # i find that .55 gives a good balance of not-too-small oceans but also cool islands
k = 1 # "cloudiness", probably don't touch this
pirate_adventure = False # arr
rockiness = 64  

roundness = 0.1
sharpness = 3
bubbliness = 4
pyramidness = 8

spikiness = 17
ranginess = 10 # number of ranges
craggliness = 40 # number of peaks per range
flatness = 18
appalachianity = 12 # distance between peaks
whorliness = 0.1 # spinniness of mountain ranges
img = []
oceanSize = 1/oceanSize
random.seed(seed)
opensimplex.seed(seed)
whorliness = whorliness*2*math.pi
if pirate_adventure:
    sigma = 1
    oceanSize = .8 
    rockiness = 30
    ranginess = random.randint(1, 3)
    # 0.5157647797018136

def getPixel(img, pt):
    ptN = [clamp(pt[0], 0, width-1), clamp(pt[1], 0, height-1)]
    try:
        return img[ptN[1]][ptN[0]]
    except:
        return [0, 0, 0]

def setPixel(img, pt, px):
    px = [clean(px[0]), clean(px[1]), clean(px[2])]
    img[pt[1]][pt[0]] = px

def getPoints(p):
    pts = []
    for v in p:
        pts.append(v[0])
    return pts

def clean(x):
    # print(x)
    return clamp(round(x), 0, 255)

def getAngle(p1, p2):
    if p1 == p2:
        return 0
    opp = p2[1] - p1[1]
    adj = p2[0] - p1[0]
    try:
        a = math.atan(abs(opp)/abs(adj))
    except ZeroDivisionError as e:
        if opp < 0:
            return 3*math.pi/2
        else:
            return math.pi/2
    if opp <= 0:
        a += 2*math.pi - 2*a
    if adj <= 0:
        a += math.pi - 2*a
    return a

def blur(img):
    print("eroding mountains...")
    for h in range(height):
        for w in range(width):
            neighbors = [getPixel(img, (w, h))]
            neighbors.append(getPixel(img, (w, h-1)))
            neighbors.append(getPixel(img, (w, h+1)))
            neighbors.append(getPixel(img, (w-1, h)))
            neighbors.append(getPixel(img, (w-1, h-1)))
            neighbors.append(getPixel(img, (w-1, h+1)))
            neighbors.append(getPixel(img, (w+1, h)))
            neighbors.append(getPixel(img, (w+1, h-1)))
            neighbors.append(getPixel(img, (w+1, h+1)))
            r = 0
            g = 0
            b = 0
            for i in range(9):
                r += neighbors[i][0]
                g += neighbors[i][1]
                b += neighbors[i][2]
            r /= 9
            g /= 9
            b /= 9
            r = round(r)
            g = round(g)
            b = round(b)
            setPixel(img, (w, h), [r, g, b])
    return img

def coastline(img, cutoff, r=15):
    filtered = img.copy()
    coastlinePts = []
    waterpts = []
    landpts = []
    if r < 1:
        raise ValueError("coastline filter radius", r, "is less than 1")
    for h, v in enumerate(img):
        l = math.sin(h)
        for w in range(width):
            m = math.cos(w)
            n = random.gauss(0.5, 0.2)
            currPt = [w, h]
            if img[h][w][1] < cutoff + r+l*r*m*n:
                if img[h][w][1] > cutoff -r+l*r*m*n:
                    coastlinePts.append(currPt)
                else:
                    waterpts.append(currPt)
            else:
                landpts.append(currPt)
    return [img, coastlinePts, waterpts, landpts]

def clamp(v, min, max):
    if v >= min:
        if v <= max:
            return v
        return max
    return min

def randomPoint():
    return [round(random.gauss(width/2, width/sigma)), round(random.gauss(height/2, height/sigma))]

def distance(p1, p2):
    if p1 == p2:
        return 0
    return math.sqrt((p1[0] - p2[0])**2 + (p1[1] - p2[1])**2)

def findClosestPoint(point, points, n=1):
    closests = []
    searchpoints = points.copy()
    minPoint = point
    minDist = (width*height)
    for x in range(n-1):
        pn = findClosestPoint(point, searchpoints, n-1)
        for i in pn:
            closests.append(i)
            if i in searchpoints:
                searchpoints.remove(i)
    for p in searchpoints:
        if (abs(p[0]-point[0]) < width / 2):
            if point == p:
                continue
            d = distance(point, p)
            if d < minDist:
                minDist = d
                minPoint = p
    closests.append(minPoint)
    return closests
39
def buildTerrain():
    img = []
    numberofPoints = clamp(round(width*height*wetness),10, round(math.sqrt(width**2+height**2)))
    print("Number of initial nodes:", numberofPoints)
    points = []
    for x in range(numberofPoints):
        points.append(randomPoint())
    linePoints = []
    pta = points[0]
    endpoint = pta
    linePoints.append(pta)
    pointsMutable = points.copy()
    for x in range(round(clamp(random.gauss((density/10)*numberofPoints, sigma), 0, numberofPoints))):
        ptb = findClosestPoint(pta, pointsMutable)[0]
        if pta in pointsMutable:
            pointsMutable.remove(pta)
            pta = ptb
            linePoints.append(ptb)

    total = height * width
    pixels = 0
    completion = 0
    for y in range(height):
        row = []
        for x in range(width):
            if pirate_adventure:
                anchor = findClosestPoint((x, y), points)[0]
            else:
                anchor = findClosestPoint((x, y), linePoints)[0]
            dd = round(distance((x, y), anchor))
            d = round(dd*oceanSize)
            r = 0
            g = round(clamp(d**2/10, 0, 255))
            b = round(clamp(255-d**2/10, 0, 255))
            row.append([r, g, b])
            pixels += 1
            if(round(pixels/total*10) > completion):
                completion = round(pixels/total*10)
                s = "="*completion + "_"*(10-completion)
                print("drawing coastlines...["+s+"]")
        img.append(row)
    print("eroding the sea floor...")
    blur(img)
    blur(img)
    blur(img)
    blur(img)
    blur(img)
    blur(img)

    v = coastline(img, 128)
    print("Final node count:", len(linePoints))
    return v

def elevation(w):
    # filtered, coastlinePts, waterpts, landpts
    land = w[1] + w[3]
    water = w[2]
    land = sorted(land)
    mangrovePts = []
    rangePts = []
    if len(land) > 0:
        # points = random.sample(land, k=round(rockiness/100*len(land)))
        for x in range(round(random.gauss(ranginess*width/128, sigma))):
            pt0 = random.sample(land, k=1)[0]
            a = random.random()*math.pi*2
            for y in range(round(random.gauss(craggliness, craggliness/3))):
                for z in range(100):
                    pt1 = random.sample(land, k=1)[0]
                    if distance(pt0, pt1) < appalachianity:
                        a2 = getAngle(pt0, pt1)
                        if a2 < a + whorliness and a2 > a - whorliness:
                            rangePts.append(pt1)
                            rangePts.append([clamp(pt1[0]+random.randint(1, spikiness), 0, width-1), clamp(pt1[1]+random.randint(1, spikiness),0,height-1)])
                            pt0 = pt1
                            a = a2
                            break
        total = len(land)
        pixels = 0
        completion = 0
        for point in land:
            if point in rangePts:
                ref = [point[0], point[1]+1]
            else:
                ref = point
            anchor = findClosestPoint(ref, rangePts, 2)
            anchor1 = anchor[0]
            anchor2 = anchor[1]
            d1 = distance(ref, anchor1)
            d2 = distance(ref, anchor2)
            h1 = 255-clean(255*(d1*flatness/width))
            h2 = 255-clean(255*(d2*flatness/width))
            h3 = 255-clean(255*opensimplex.noise2(ref[0]/256, ref[1]/256))
            h4 = getPixel(w[0], point)[0]
            h = (h1*roundness + h2*sharpness+h3*bubbliness+h4*pyramidness)/(roundness+sharpness+bubbliness+pyramidness)

            h/= 256

            setPixel(w[0], point, color(h)[0])
            pixels += 1
            if(round(pixels/total*10) > completion):
                completion = round(pixels/total*10)
                s = "="*completion + "_"*(10-completion)
                print("piling up hills...["+s+"]")






    return map

def color(h):
    if h < 0:
        water = 1
    else:
        water = 0
    if bw:
        h /= 2
        h += .5
        return [clean(h*255), clean(h*255), clean(h*255)], water
    # if h < 0:
        # return [0, 0, 255], 1
    # else:
        # return [0, 255, 0], 0

    #0de5a8"(128), "#0d b6 e5"(192), "#  1cefe5"(256)
    if h <= 0 and h > -0.5:
        h *= 2
        r = clean(26*(-h) + 34*(1+h))
        g = clean(56*(-h) + 106*(1+h))
        b = clean(167*(-h) + 138*(1+h))
        water = 1
    elif h < -.5:
        h += 0.5
        h*=2
        r = clean(18*(-h) + 26*(1+h))
        g = clean(9*(-h) + 56*(1+h))
        b = clean(160*(-h) + 167*(1+h))
        water = 1

    elif h < 0.5:
        h *= 2
        r = clean(27*(1-h) + 155*(h))
        g = clean(124*(1-h) + 143*(h))
        b = clean(59*(1-h) + 78*(h))
    else:
        h -= 0.5
        h *= 2
        r = clean(155*(1-h) + 84*(h))
        g = clean(143*(1-h) + 72*(h))
        b = clean(78*(1-h) + 48*(h))
    return [[r, g, b], water]

scale = 2
octaves = 4
islandiness = 1
continentalness = 1
offset = -128
thirstiness = .25
# opensimplex.seed(2433825)
coastDepth = 0.1
def simplexTerrain():
    waterRatio = 0
    waterPts = []
    landPts = []
    coastlinePts = []
    o = 1
    v = 0
    w = 0
    pixels = 0
    total = width*height
    completion = 0
    for y in range(height):
        img.append([])
        for x in range(width):
            for n in range(octaves):
                v += (opensimplex.noise2(x/128.0*(2**n), y/128.0*(2**n))+.5)/(2**n)
                w += (opensimplex.noise2(-x/32.0*(2**n), -y/32.0*(2**n))-1)/(2**n)

            v /= 2
            w /= 2

            z = clamp((v+w)/2+thirstiness, -1, 1)
            if z <= 0:
                waterPts.append([x, y])
            elif z >= 0+coastDepth:
                landPts.append([x, y])
            else:
                coastlinePts.append([x, y])
                
            px = color(z)
            waterRatio += px[1]
            if z > 0:
                z = clean(z*255)
                px = [z, z, z], 0
            # px = [clean(z*255),clean(z*255),clean(z*255)]
            img[y].append(px[0])
            pixels += 1
            if(round(pixels/total*10) > completion):
                completion = round(pixels/total*10)
                s = "="*completion + "_"*(10-completion)
                print("drawing coastlines...["+s+"]")
    return [img, coastlinePts, waterPts, landPts]
    print("water ratio: " +str(round(100*waterRatio/width/height))+"%")


def bake(map):
    img = map[0]
    bakedImg = []
    for k, row in enumerate(img):
        bakedImg.append(())
        for px in row:
            bakedImg[k] = bakedImg[k] + tuple(px)
    map[0] = bakedImg


# map = buildTerrain()
map = simplexTerrain()
map = elevation(map)

bake(map)


with open('out.png', 'wb') as f:
    w = png.Writer(width, height, greyscale=False)
    w.write(f, map[0])
print("seed:",seed)