all:
	python convert.py
	ebb img/*.png
	platex     draft_final
	platex     draft_final
	platex 	   draft_final		
	dvipdfmx  -p a4 draft_final
	open draft_final.pdf
clean:
	/bin/rm -f *~ *.log *.dvi *.blg *.aux *.out *.blb draft_final.pdf
