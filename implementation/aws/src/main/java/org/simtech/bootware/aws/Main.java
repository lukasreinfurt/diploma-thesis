package org.simtech.bootware.aws;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Ec2Instance ec2 = new Ec2Instance();
		ec2.create();

		System.out.println("Press <ENTER> to terminate the EC2 instance.");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();

		ec2.terminate();
	}

}
