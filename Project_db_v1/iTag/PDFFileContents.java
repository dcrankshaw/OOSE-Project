package iTag;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "PDFFileContents" )
public class PDFFileContents implements FileContents {

	@Override
	public int search(String searchTerm) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public FileContents getPDFDate() {
		return new PDFFileContents();
	}
}
