import xlrd
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

book=xlrd.open_workbook("budget20132014.xlsx")
sheet=book.sheet_by_index(0)
for ridx in xrange(0,sheet.nrows):
        print ridx
	row = sheet.row(ridx)
	for code_idx in [0,2,4]:
                try:
                        code = int(row[code_idx].value)
                except:
                        continue
                code = str(code)
                code = "0"*(4+code_idx-len(code)) +code
                title = row[code_idx+1].value
                for year,row_idx in [[2013,6],[2014,11]]:
                        try:
                                amount = int(row[row_idx].value)
                        except:
                                amount = 0
                        key = "%s/%s" % (year,code)
                        sums.setdefault(key,{'code':code,'year':year,'title':title,'net_allocated':0})
                        sums[key]['net_allocated'] += amount

out = file("out.json","w")
keys = sums.keys()
keys.sort()
for key in keys:
	out.write("%s\n" % json.dumps(sums[key]))

