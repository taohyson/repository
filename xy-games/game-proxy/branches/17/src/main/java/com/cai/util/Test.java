package com.cai.util;

import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

public class Test {

	public static void main(String[] args) {

		
//		Set<String> ipSet = new HashSet<>();
//		Set<String> ipSet2 = new HashSet<>();
//		
//		FileInputStream fis = null;
//		InputStreamReader isr = null;
//		BufferedReader br = null; // 用于包装InputStreamReader,提高处理性能。因为BufferedReader有缓冲的，而InputStreamReader没有。
//		try {
//			String str = "";
//			String str1 = "";
//			fis = new FileInputStream("./config/ip_addr.txt");// FileInputStream
//			// 从文件系统中的某个文件中获取字节
//			isr = new InputStreamReader(fis);// InputStreamReader 是字节流通向字符流的桥梁,
//			br = new BufferedReader(isr);// 从字符输入流中读取文件中的内容,封装了一个new
//			
//			int count = 0;
//			while ((str = br.readLine()) != null) {
//				count++;
//				ipSet.add(str);
//				
//				String[] s2 = str.split("\\.");
//				ipSet2.add(s2[0]+"."+s2[1]+"."+s2[2]);
//			}
//			System.out.println("ok.........."+ipSet.size() + "===" + ipSet2.size());
//		} catch (FileNotFoundException e) {
//			System.out.println("找不到指定文件");
//		} catch (IOException e) {
//			System.out.println("读取文件失败");
//		} finally {
//			try {
//				br.close();
//				isr.close();
//				fis.close();
//				// 关闭的时候最好按照先后顺序关闭最后开的先关闭所以先关s,再关n,最后关m
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
//		String s1 = "";
//		String s2 = null;
//		
//		System.out.println(s1.equals(s2));
//		
//				
//		
//		System.out.println(DigestUtils.md5Hex("QNKCDZO"));
		
		System.out.println("1.0.29".compareTo("1.0.30"));
		
	}

}
