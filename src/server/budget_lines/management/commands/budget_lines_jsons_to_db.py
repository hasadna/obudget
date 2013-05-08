import sys
import csv
import re
from budget_lines.models import BudgetLine 
from django.core.management.base import BaseCommand
import json

class Command(BaseCommand):

    args = '[update] <jsons-file> <year1> <year2> ...'
    help = 'Transfers a sorted jsons budget data files into the DB'

    def handle(self, *args, **options):

        print 'Loading raw rows'
        k = 0
        update = args[0] == 'update'
        if update:
            args = args[1:]

        filename = args[0]
        years = args[1:]
        years = [ int(y) for y in years ]

        print filename
        for l in file(filename).readlines():
            d = json.loads(l)
            if len(years)>0:
                if d['year'] not in years:
                    continue

            if update:
                bl,_ = BudgetLine.objects.get_or_create( budget_id=d['code'],
                                                         year = d['year'],
                                                         defaults = { 'title' : d['title'] } )
                bl.title = d['title']
                bl.net_amount_allocated = d.get('net_allocated')
                bl.net_amount_revised = d.get('net_revised')
                bl.net_amount_used = d.get('net_used')
                bl.gross_amount_allocated = d.get('gross_allocated')
                bl.gross_amount_revised = d.get('gross_revised')
                bl.gross_amount_used = d.get('gross_used')
                bl.budget_id_len = len(d['code'])
                bl.save()                
                
            else:
                bl = BudgetLine( budget_id=d['code'],
                                 year = d['year'],
                                 title = d['title'],
                                 net_amount_allocated = d.get('net_allocated'),
                                 net_amount_revised = d.get('net_revised'),
                                 net_amount_used = d.get('net_used'),
                                 gross_amount_allocated = d.get('gross_allocated'),
                                 gross_amount_revised = d.get('gross_revised'),
                                 gross_amount_used = d.get('gross_used'),
                                 budget_id_len = len(d['code'])
                                 )
                bl.save()
            if bl.budget_id_len == 2:        
                continue
                
            for i in range(2,bl.budget_id_len,2):
                parent_budget_id  = bl.budget_id[:-i]
                try:
                    parent = BudgetLine.objects.get( year = bl.year, budget_id = parent_budget_id, budget_id_len = len(parent_budget_id) )
                    bl.containing_line = parent
                    bl.save()
                    break
                except:
                    continue

            k+=1
            if k % 1000 == 0:
                print k
        
