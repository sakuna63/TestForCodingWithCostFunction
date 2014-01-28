all:
	python convert.py
	ebb img/*.png
	platex     report
	platex     report
	platex 	   report		
	dvipdfmx  -p a4 report
	open report.pdf
clean:
	/bin/rm -f *~ *.log *.dvi *.blg *.aux *.out *.blb report.pdf
