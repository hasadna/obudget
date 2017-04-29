==============================
Open Budget of the Israeli Gov
==============================

## This is the OLD website. For current development endeavors you should go to http://github.com/OpenBudget/BudgetKey

This projects makes it easy for citizens and their serveris to access the Israeli Budget. Based on files shared by the `Accountant General`_, the system currently holds data from 1992 to 2012. 

.. _`Accountant General`: http://www.ag.mof.gov.il/AccountantGeneral/Templates/BudgetExecution/PublicationFiles.aspx?NRMODE=Published&NRNODEGUID=%7b67B554DC-C476-4966-82A4-836E1201DF2B%7d&NRORIGINALURL=%2fAccountantGeneral%2fBudgetExecution%2fBudgetExecutionTopNav%2fBEHistoryData%2f&NRCACHEHINT=Guest

Other sources can be found `here`_.

.. _`here`: https://track.nsa.co.il/projects/obudget/wiki/%D7%9E%D7%A7%D7%95%D7%A8%D7%95%D7%AA

INSTALL
-------

::

	$ python bootstrap.py
	$ bin/buildout
	$ bin/test
	$ bin/django syncdb --migrate
	$ bin/django budget_lines_jsons_to_db data/master.json

If you'd like to work on the GWT based user interfce you'll have to install the GWT_ and `gwt-visualization`_ and update the path reference in src/obudget/gwt/src/build.xml.

.. _GWT: http://code.google.com/webtoolkit/download.html
.. _`gwt-visualization`: http://code.google.com/p/gwt-google-apis/downloads/list
.. _`gwt-search`: http://code.google.com/p/gwt-google-apis/downloads/list


UI
--

Point your browser at http://127.0.0.1:8000/gwt/obudget.html

To see the embed interface open the file found in src/samples/post.html

API
---

Currently the system includes a simple API to get data on a known budget row or section. Full `API documentation`_ can be found in our wiki.

.. _`api documentation`: https://track.nsa.co.il/projects/obudget/wiki/%D7%9E%D7%9E%D7%A9%D7%A7_%D7%9E%D7%99%D7%93%D7%A2


Example:
~~~~~~~~

To get the total and breakdown for the fire depatment::
	$ curl http://127.0.0.1:8000/000610

