
all: jpedal_test.java pdfpanel.java
	javac -classpath ../libraries/jpedal_lgpl.jar jpedal_test.java pdfpanel.java


run: all
	java -cp ../libraries/jpedal_lgpl.jar:. jpedal_test /Users/paul/Downloads/lec10.algstructure.pdf

scroll_test.class: scroll_test.java
	javac scroll_test.java
scroll_test: scroll_test.class