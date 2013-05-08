### encoding: utf8 ###
from django.db import models

INFLATION = {1992: 2.338071159424868,
 1993: 2.1016785142253185,
 1994: 1.8362890269054741,
 1995: 1.698638328862775,
 1996: 1.5360153664058611,
 1997: 1.4356877762122495,
 1998: 1.3217305991625745,
 1999: 1.3042057718241757,
 2000: 1.3042057718241757,
 2001: 1.2860800081392196,
 2002: 1.2076314957018655,
 2003: 1.2308469660644752,
 2004: 1.2161648953888384,
 2005: 1.1878270593983091,
 2006: 1.1889814138002117,
 2007: 1.1499242230869946,
 2008: 1.1077747422214268,
 2009: 1.0660427753379829,
 2010: 1.0384046275616676,
 2011: 1.0163461588107117,
 2012: 1.0,
 2013: 1.0,
 2014: 1.0,
}

class BudgetLine(models.Model):
    
    title               = models.CharField( max_length=256, db_index=True )
    budget_id           = models.CharField( max_length=64,db_index=True )
    budget_id_len       = models.PositiveIntegerField( default=0, db_index=True )
    
    net_amount_allocated    = models.IntegerField( null = True )
    net_amount_revised        = models.IntegerField( null = True )
    net_amount_used         = models.IntegerField( null = True ) 
    gross_amount_allocated    = models.IntegerField( null = True )
    gross_amount_revised        = models.IntegerField( null = True )
    gross_amount_used         = models.IntegerField( null = True ) 
    
    year                = models.PositiveIntegerField( db_index=True )
    
    containing_line     = models.ForeignKey( 'self', related_name='sublines', null=True, db_index=True )

    @property
    def inflation_factor(self): return INFLATION.get(self.year, 1)

    def _amount_allocated(self):
        return "<label title='סכום לפני התחשבות במדד: %s'>%s</label>" % ( self.amount_allocated, int(self.inflation_factor*self.amount_allocated) ) 
    _amount_allocated.short_description = u'הקצאה (באלפי \u20aa)'
    _amount_allocated.allow_tags = True

    def _amount_revised(self):
        return "<label title='סכום לפני התחשבות במדד: %s'>%s</label>" % ( self.amount_revised, int(self.inflation_factor*self.amount_revised) ) 
    _amount_revised.short_description = u'הקצאה מעודכנת (באלפי \u20aa)'
    _amount_revised.allow_tags = True

    def _amount_used(self):
        return "<label title='סכום לפני התחשבות במדד: %s'>%s</label>" % ( self.amount_used, int(self.inflation_factor*self.amount_used) ) 
    _amount_used.short_description = u'ניצול (באלפי \u20aa)'
    _amount_used.allow_tags = True
    
    def _title(self):
        return "<a href='/admin/budget_lines/budgetline/my/?q=%s&year=%s&ot=asc&o=1'>%s</a>" % (self.budget_id, self.year, self.title )
    _title.short_description = u'שם'
    _title.allow_tags = True
    
    def parent_budget_id(self):
        if self.containing_line == None:
            return u'אין'
        else:
            return "<a href='/admin/budget_lines/budgetline/my/?q=%s&year=%s&ot=asc&o=1'>%s</a>" % (self.containing_line.budget_id, self.containing_line.year, self.containing_line.title)
    parent_budget_id.short_description = u'סעיף אב'
    parent_budget_id.allow_tags = True
