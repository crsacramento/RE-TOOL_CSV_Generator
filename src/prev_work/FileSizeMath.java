package prev_work;
import java.io.File;
import java.util.ArrayList;



public class FileSizeMath {

	/**
	 * @param args
	 */
	
	//devolve a média do tamanho dos ficheiros de texto na pasta "path"
	public static double calculateAverageFileSize(String path)
	{
		// Directory path here
		 
		  String files;
		  File folder = new File(path);
		  File[] listOfFiles = folder.listFiles(); 
		  
		  ArrayList<Double> fileSizes=new ArrayList<Double>(); 
		 
		  for (int i = 0; i < listOfFiles.length; i++) 
		  {
		 
		   if (listOfFiles[i].isFile()) 
		   {
		   files = listOfFiles[i].getName();
		       if (files.endsWith(".txt") || files.endsWith(".TXT"))
		       {
		          System.out.println(files);
		          fileSizes.add((double)listOfFiles[i].length());
		        }
		     }
		  }
		  
		  double average=0;
		  
		  for (int i=0; i!=fileSizes.size(); i++)
		  {
			  average+=fileSizes.get(i);
		  }
		  
		  average/=fileSizes.size();
		
		
		return average;
		
	}
	
	public static double calculateFileSizeByFileIndexAndCompareWithAverage(int i, double average)
	{
		File file = new File(System.getProperty("user.dir")+"\\HTMLfinal\\"+i+".txt");
		 
		double filesize = (double)file.length();
		  
		return filesize/average;
		
	}
	
	public static double compareHTMLSizeWithPreviousFile(int index1, int index2)
    {
		File file = new File(System.getProperty("user.dir")+"\\HTMLtemp\\"+index1+".txt");
		double filesize = (double)file.length();
		//System.out.println("AAAAAAA: " + filesize);
		
		File fileRevised = new File(System.getProperty("user.dir")+"\\HTMLtemp\\"+index2+".txt");
		double filesizeRevised = (double)fileRevised.length();
		//System.out.println("BBBBBB: " + filesizeRevised);
		
		//Divide por 1000 para dar a diferenca em kb; otherwise dá em bytes 
		//e diferenca entre dois files de 98kb pode ser de 400 bytes
		//dando uma nocao inconclusiva da diferenca dos dois files
		return (filesizeRevised-filesize)/1000;
		
    }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
