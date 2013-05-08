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

book=xlrd.open_workbook("execution2012.xlsx")
sheet=book.sheet_by_index(0)
for ridx in xrange(1,sheet.nrows):
	row = sheet.row(ridx)
#	code1 = None
	for code_idx in range(1,8,2):
		code = ("%%0%dd" % (code_idx+3)) % int(row[code_idx].value)
#		if code_idx != 1: 
#			code = code1+code
#		else:
#			code1 = code
		x={}
		x['code']=code
		x['year']=2012
		x['title']=row[code_idx+1].value
		x['net_used'] = get_from(row[25].value)
		x['net_allocated'] = x['net_revised'] = get_from(row[11].value)
		x['net_revised'] += get_from(row[12].value)
		x['gross_allocated'] = x['gross_revised'] = get_from(row[18].value)
		x['gross_revised'] += get_from(row[19].value)

		sums.setdefault(code,{})
		sums[code]=sumdict(sums[code],x)
		print code,sums[code]

out = file("out.json","w")
keys = sums.keys()
keys.sort()
for key in keys:
	out.write("%s\n" % json.dumps(sums[key]))

