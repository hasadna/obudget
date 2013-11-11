import csv
import json

def get_from(x):
	return 0 if x=='' else int(x)

def sumdict(a,b):
	for k,v in b.iteritems():
		if k in ['code','year','title']: 
			a[k] = v
			continue
		a.setdefault(k,0)
		a[k]+=v
	ret = {}
	ret.update(a)
	return ret

sums = {}

budgets=csv.reader(file('budgets20132014.csv'))
for row in budgets:
    try:
        year = int(row[0])
    except:
        continue
    for col in [1,3,5,7]:
        code = "00"+row[col].replace('-','')
        title = row[col+1].decode('utf8')
        try:
            net_amount = int(row[11].replace(",",""))
        except:
            net_amount = 0
        try:
            gross_amount = net_amount + int(row[12].replace(",",""))
        except:
            gross_amount = net_amount
        
        key = "%s/%s" % (year,code)
        sums.setdefault(key,{'code':code,'year':year,'title':title,'net_allocated':0,'gross_allocated':0})
        sums[key]['net_allocated'] += net_amount
        sums[key]['gross_allocated'] += gross_amount

out = file("out.json","w")
keys = sums.keys()
keys.sort()
for key in keys:
	out.write("%s\n" % json.dumps(sums[key]))

