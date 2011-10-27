#! /bin/sh

cd Biblio/src
java -classpath ../../libraries/jpedal_lgpl.jar:../../libraries/hibernate3.jar:../../libraries/javax.persistence.jar:. edu.jhu.cs.oose.biblio.gui.tests.ImportDialogGUITest
