package demo;

import java.util.Scanner;

public class DemoApplication {

	public static void main(String[] args) throws Exception {

		System.out.println(" Enter 1 for json generation ");
		try (Scanner scanner = new Scanner(System.in);) {
			int code = scanner.nextInt();
			if (code == 1) {
				CMU2JsonConvertor.generateJson();
			} else {
				System.err.println("Invalid entry : " + code);
			}
		}

	}
}
