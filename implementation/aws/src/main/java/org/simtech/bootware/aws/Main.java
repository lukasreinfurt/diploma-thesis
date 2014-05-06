package org.simtech.bootware.aws;

import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Ec2Instance ec2 = new Ec2Instance();
		ec2.create();

		Scanner sc = new Scanner(System.in);
		System.out.println("Press <ENTER> to connect via SSH.");
		sc.nextLine();

		SSH ssh = new SSH();
		ssh.connect(ec2.getPublicDNS(), "ec2-user", ec2.getPrivateKey());

		System.out.println("Press <ENTER> to execute commands.");
		sc.nextLine();

		//ssh.upload("aws-0.0.1.jar", "/home/ec2-user/");
		ssh.execute("uname -a && date && uptime && who");
		ssh.disconnect();

		System.out.println("Press <ENTER> to terminate the EC2 instance.");
		sc.nextLine();

		ec2.terminate();
	}

}
