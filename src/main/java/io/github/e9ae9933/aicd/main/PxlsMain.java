package io.github.e9ae9933.aicd.main;

import io.github.e9ae9933.aicd.NoelByteBuffer;
import io.github.e9ae9933.aicd.Utils;
import io.github.e9ae9933.aicd.pxlskiller.PxlCharacter;
import io.github.e9ae9933.aicd.pxlskiller.Settings;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PxlsMain
{
	public static void main(String[] args) throws Exception
	{
		JFileChooser c=new JFileChooser();
		c.setCurrentDirectory(new File(""));
		c.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.getName().toLowerCase().endsWith(".pxls")||f.isDirectory();
			}

			@Override
			public String getDescription()
			{
				return ".pxls";
			}
		});
		c.setFileSelectionMode(JFileChooser.FILES_ONLY);
		c.setMultiSelectionEnabled(true);
		c.showDialog(null,null);
		File[] files=c.getSelectedFiles();
//		filess
		Arrays.stream(files).parallel()
				.forEach(file->{
					handle(file)
//					handle(file);
					//handlke()
				;});
//		for(File file:files)
//			handle(file);
	}
	static void handle(File file)
	{
		System.out.println("[%s] file %s start".formatted(Thread.currentThread().getName(),file.getName()));
		NoelByteBuffer b=new NoelByteBuffer(Utils.readAllBytes(file));
		Settings s=new Settings();
		s.pxlsName=file.getName();
		s.externalResourcesDir=file.getParentFile();
		s.customHeader="konnna_syarinn_no_saikaihatsu_ha_okashii_by_cloba_u".getBytes(StandardCharsets.UTF_8);
		PxlCharacter character=new PxlCharacter(b,s);
		byte[] out=character.outputAsBytes();
		File fileName;
		if(file.getName().endsWith(".pxls"))
			fileName=new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 1));
			else fileName=new File(file.getAbsolutePath()+".pxl");//.)
		Utils.writeAllBytes(fileName,out);
		System.out.println("[%s] file %s end".formatted(Thread.currentThread().getName(),file.getName()));
	}
}
