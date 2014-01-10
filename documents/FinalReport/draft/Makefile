all:
	python convert.py
	ebb img/*.png
	platex     draft
	jbibtex    draft
	platex     draft
	platex 	   draft		
	dvipdfmx  -p a4 draft
	open draft.pdf
clean:
	/bin/rm -f *~ *.log *.dvi *.blg *.aux *.out *.blb draft.pdf
