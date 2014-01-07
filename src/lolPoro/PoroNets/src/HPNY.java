
public class HPNY {

	public static void main(String[] args) {
		String first = "";
		String second = "Happy New Year";
		
		while(true)
		{
			if(first.length() < 50 )
			{
				first += "+";
			}
			else
			{
				first = "";
			}
			System.out.println(first + second);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		

	}

}
