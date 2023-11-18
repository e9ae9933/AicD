package io.github.e9ae9933.aicd.main;

import io.github.e9ae9933.aicd.l10nkiller.Family;
import io.github.e9ae9933.aicd.l10nkiller.Main;
import io.github.e9ae9933.aicd.l10nkiller.MultiLanguageFamilies;
import io.github.e9ae9933.aicd.l10nkiller.RefreshedEventLoader;
import org.apache.commons.lang3.Validate;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class L10nMain
{
	public static void main(String[] args) throws Exception
	{
		JFileChooser c=new JFileChooser();
		c.setCurrentDirectory(new File("."));
		c.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return f.getName().equalsIgnoreCase("AliceInCradle.exe")||f.isDirectory();
			}

			@Override
			public String getDescription()
			{
				return "AliceInCradle.exe";
			}
		});
		c.setFileSelectionMode(JFileChooser.FILES_ONLY);
		c.setMultiSelectionEnabled(false);
		c.showDialog(null,null);
		File aic=c.getSelectedFile().getParentFile();

		Validate.isTrue(new File(aic,"redirect").isDirectory(),"redirect folder  not found");
		File l10n=new File(aic,"redirect/l10n");
//		Validate.isTrue(l10n.listFiles()==null||l10n.listFiles().length==0,l10n+" exists and not blank");
		l10n.mkdir();
		handleEv(aic);
		handleTx(aic);
	}
	static void handleTx(File dir) throws Exception
	{
		Main.main(new String[]{
				"--list",new File(dir,"redirect/__tx_list").getAbsolutePath(),
				"--dir",new File(dir,"AliceInCradle_Data/StreamingAssets/localization").getAbsolutePath(),
				"--output",new File(dir.getAbsolutePath()+"/redirect/l10n").getAbsolutePath()
		});
	}
	static void handleEv(File dir) throws Exception
	{
		MultiLanguageFamilies mlf=RefreshedEventLoader.loadWholeFromAIC(dir);
		RefreshedEventLoader.writeMultiLanguageFamiliesToDir(new File(dir,"redirect/l10n"),mlf);
	}
}
