all:
	python convert.py
	ebb img/*.png
	platex     report
	platex 	   report		
	dvipdfmx  -p a4 report
	open report.pdf
	cp report.pdf ~/Dropbox/
clean:
	/bin/rm -f *~ *.log *.dvi *.blg *.aux *.out *.blb *.toc *.bbl report.pdf
