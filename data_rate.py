import os
import re

f = open('first_plaintext.txt', 'r')

contents = f.read()
rate = re.compile('Data Rate: [0-9]*\.[0-9]')
allRates = rate.findall(contents)

l =[]

print len(allRates)

for x in range(len(allRates)):
    for t in allRates[x].split():
        try:
            l.append(float(t))
        except ValueError:
            pass

l.sort()
for i in range (0,5):
    print l[i]
    
print sum(l)/float(len(l))
