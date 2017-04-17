package com.vag.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public class FTPClient {

	private Socket socket;
	private Socket dataSocket;
	public void sendOutputToConsole(String command)
	{
		//System.out.println("> " + command);
	}


	public String readInputFromConsoleNonRecursive()
	{
		String s="";

		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			s = bufferRead.readLine();
			//System.out.println(response );
			//bufferRead.close();


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	public String readInputFromConsole()
	{
		String s="";
		String response = "";
		try {

			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			s = bufferRead.readLine();
			if("quit".equals(s))
			{
				System.out.println("GoodBye");
				sendCommandToServer(s);
				response = getResponseFromFTPServer();
				return "";
			}
			if("pwd".equals(s))
			{
				sendCommandToServer(s);
				response = getResponseFromFTPServer();
				System.out.println(response );

			}
			else
			{
				sendCommandToServer(s);
				response = getResponseFromFTPServer();
				System.out.println(response );
			}
			//bufferRead.readLine();
			//bufferRead.close();
			readInputFromConsole();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	public String sendCommandToServer(String command)
	{
		String response = "";
		try {
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream()));
			writer.write(command+ "\r\n");
			writer.flush();
			sendOutputToConsole(command);  


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return response;
	}

	public String getResponseFromFTPServer()
	{

		String response = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			response = reader.readLine();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return response;

	}

	public String getDataResponseFromFTPServerAsString()
	{

		String response = "";
		try {
			//if(dataSocket == null)
			dataSocket = new Socket(credHost, credPort);
			//BufferedReader reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
			//response = reader.readLine();

			//read control response
			response = this.getResponseFromFTPServer();
			System.out.println(response);

			BufferedInputStream bif = new BufferedInputStream(dataSocket.getInputStream());
			byte[] buffer = new byte[1024*10];
			StringBuffer sbuf = new StringBuffer();
			while(bif.read(buffer) > -1){
				sbuf.append(new String(buffer));
			}
			response = sbuf.substring(0);

			dataSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return response;

	}

	public int readRemoteFile(String filename)
	{
		int numBytes = 0;
		try {
			dataSocket = new Socket(credHost, credPort);

			//read control response
			String response = this.getResponseFromFTPServer();
			System.out.println(response);

			if(!response.startsWith("5")){

				BufferedInputStream bif = new BufferedInputStream(dataSocket.getInputStream());
				byte[] buffer = new byte[1024*10];
				File file = new File(this.localDir + this.fileSep + filename);
				FileOutputStream fos = new FileOutputStream(file);
				int num;
				while((num = bif.read(buffer)) > -1){
					fos.write(buffer);
					numBytes += num;
				}
				fos.flush();
				fos.close();
			}
			dataSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {

		}


		return numBytes;
	}

	public int sendLocalFile(String filename)
	{
		int numBytes = 0;
		try {
			
			File file = new File(this.localDir + this.fileSep + filename);
				dataSocket = new Socket(credHost, credPort);

				//read control response
				String response = this.getResponseFromFTPServer();
				System.out.println(response);

				if(!response.startsWith("5")){
					FileInputStream localInputStream = new FileInputStream(file);
					//BufferedInputStream bif = new BufferedInputStream(dataSocket.getInputStream());
					byte[] buffer = new byte[1024*10];

					BufferedOutputStream remoteOutputStream = new BufferedOutputStream(dataSocket.getOutputStream());
					int num;
					while((num = localInputStream.read(buffer)) > -1){
						remoteOutputStream.write(buffer);
						numBytes += num;
					}
					remoteOutputStream.flush();
					
					localInputStream.close();
				}
				dataSocket.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {

		}


		return numBytes;
	}

	private boolean isLocalFile(String filename)
	{
		File file = new File(this.localDir + this.fileSep + filename);
		return file.exists();
	}
	
	private void changeLocalDir(String filename){
		File file = new File(this.localDir + this.fileSep + filename);
		if(file.exists())
		{
			this.localDir = this.localDir + this.fileSep + filename;
			System.out.println("Local directory changed to :" + this.localDir);
		}else{
			System.out.println("Invalid local directory");
		}
	}
	
	public String connectToFTPServer(String host)
	{
		//Socket socket;
		String response = "";
		try {
			socket = new Socket(host,21);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return response;

	}

	public String disconnectFromFTPServer()
	{
		this.sendCommandToServer("quit");
		String response = this.getResponseFromFTPServer();
		System.out.println(response);
		try {
			this.socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	private String credHost = null;
	private int credPort = 0;
	public void setPassiveMode(){
		try {
			this.sendCommandToServer("PASV");
			String response = this.getResponseFromFTPServer();
			while(!response.startsWith("227") || !response.startsWith("228") || !response.startsWith("229")){
				System.out.println(response);
				response = this.getResponseFromFTPServer();
			}
			System.out.println(response);
			int openBrac = response.indexOf("(");
			int closeBrac = response.indexOf(")");
			
			String serverCred = response.substring(openBrac+1,closeBrac);
			

			String[] serverCredArr = serverCred.split(",");
			credHost = serverCredArr[0] +
			"." + serverCredArr[1] +
			"." + serverCredArr[2] +
			"." + serverCredArr[3];
			int portNum1 = Integer.parseInt(serverCredArr[4]);
			int portNum2 = Integer.parseInt(serverCredArr[5]);

			credPort = ( (portNum1*256) + portNum2);

			//dataSocket = new Socket(credHost, credPort);
			//System.out.println(this.getDataResponseFromFTPServer());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.disconnectFromFTPServer();
			System.exit(-1);
		}
	}

	private String localDir;
	private String fileSep;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String host = args[0];
		System.out.println("Hello");
		FTPClient ftp = new FTPClient();	
		ftp.localDir = System.getProperty("user.dir");
		System.out.println("Local Dir :" + ftp.localDir);
		
		ftp.fileSep = System.getProperty("file.separator");
		
		
		ftp.connectToFTPServer(host);
		System.out.println(ftp.getResponseFromFTPServer());

		//USER
		System.out.println("USERNAME" );
		String input = ftp.readInputFromConsoleNonRecursive();
		ftp.sendCommandToServer("USER " + input);
		String response = ftp.getResponseFromFTPServer();
		System.out.println(response);

		//PASS
		System.out.println("PASSWORD" );
		input = ftp.readInputFromConsoleNonRecursive();
		ftp.sendCommandToServer("PASS " + input);
		response = ftp.getResponseFromFTPServer();
		System.out.println(response);


		//set connection to passive mode
		//ftp.setPassiveMode();

		if(response.indexOf("530") < 0)
		{
			do{
				input = ftp.readInputFromConsoleNonRecursive();

				if(input.equalsIgnoreCase("quit"))
				{
					ftp.disconnectFromFTPServer();
					System.exit(0);
				}
				else{

					if(input.startsWith("list") || input.startsWith("ls")){
						input = input.replaceFirst("ls", "list");
						ftp.setPassiveMode();
						ftp.sendCommandToServer(input);

						//read data
						response = ftp.getDataResponseFromFTPServerAsString();
						System.out.println(response);

						//read control
						response = ftp.getResponseFromFTPServer();
						System.out.println(response);
					}
					else if(input.startsWith("get")){
						input = input.replaceFirst("get", "retr");
						ftp.setPassiveMode();
						ftp.sendCommandToServer(input);

						String[] commandParts = input.split(" ");
						//read data
						int num = ftp.readRemoteFile(commandParts[1]);
						System.out.println("Recieved " + num + " bytes");

						//read control
						response = ftp.getResponseFromFTPServer();
						System.out.println(response);
					}
					else if(input.startsWith("put")){
						input = input.replaceFirst("put", "stor");
						String[] commandParts = input.split(" ");
						if(ftp.isLocalFile(commandParts[1])){
							System.out.println("Local file found");
							ftp.setPassiveMode();
							ftp.sendCommandToServer(input);


							//read data
							ftp.sendLocalFile(commandParts[1]);
							//System.out.println(response);

							//read control
							response = ftp.getResponseFromFTPServer();
							System.out.println(response);
						}else{
							System.out.println("Invalid local file");
						}
					}
					else if(input.startsWith("delete")){
						input = input.replaceFirst("delete", "dele");
						ftp.sendCommandToServer(input);
						response = ftp.getResponseFromFTPServer();
						System.out.println(response);
					}
					else if(input.startsWith("lcd")){
						String[] commandParts = input.split(" ");
						ftp.changeLocalDir(commandParts[1]);
					}
					else if(input.startsWith("mkdir")){
						input = input.replaceFirst("mkdir", "mkd");
						ftp.sendCommandToServer(input);
						response = ftp.getResponseFromFTPServer();
						System.out.println(response);
					}
					else if(input.startsWith("cd ")){
						input = input.replaceFirst("cd", "cwd");
						ftp.sendCommandToServer(input);
						response = ftp.getResponseFromFTPServer();
						System.out.println(response);
					}
					else{
						ftp.sendCommandToServer(input);
						response = ftp.getResponseFromFTPServer();
						System.out.println(response);
					}



				}
			}while(true);
		}
	}

}
