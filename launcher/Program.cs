using ICSharpCode.SharpZipLib.Zip;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace AicToolboxLauncher
{
	public class Program
	{
		static void Main(string[] args)
		{
			try
			{
				FileStream fs = new FileStream("session1.lock", FileMode.OpenOrCreate, FileAccess.ReadWrite, FileShare.None);
				fs.Lock(0, 0);
				AppDomain.CurrentDomain.AssemblyResolve +=
					(sender, args2) =>
					{
						string s=args2.Name;
						if(s.Contains("SharpZipLib"))
						{
							return Assembly.Load(Resource.ICSharpCode_SharpZipLib);
						}
						return null;
					};
				if (!File.Exists("aicd_resources/info.txt"))
					initResources();
				Directory.CreateDirectory("aicd_resources");
				File.WriteAllBytes("aicd_resources/AicD-all.jar", Resource.AicD_all);
				ProcessStartInfo info = new ProcessStartInfo();
				info.CreateNoWindow = true;
				info.UseShellExecute = false;
				info.WorkingDirectory = ".";
				info.FileName = "aicd_resources/jre/bin/java.exe";
				info.Arguments = "-Dfile.encoding=UTF-8 -jar AicD-all.jar";
				Process p = Process.Start(info);
			}
			catch(Exception e)
			{
				show(e);
			}
		}
		static void show(Exception e)
		{
			MessageBox.Show(
				"Something went wrong.\nDo you have the permission to the current directory?\nOr maybe you have launched another modloader." + e,
				"错误",
				MessageBoxButtons.OK,
				MessageBoxIcon.Error,
				MessageBoxDefaultButton.Button1
				);
		}
		static void initResources()
		{
			MemoryStream ms = new MemoryStream(Resource.OpenJDK17U_jre_x64_windows_hotspot_17_0_8_7);
			ZipInputStream zis = new ZipInputStream(ms);
			ZipEntry ze;
			while ((ze = zis.GetNextEntry()) != null)
			{
				string s = ze.Name;
				s = s.Substring(s.IndexOf('/') + 1);
				if (ze.IsDirectory)
					Directory.CreateDirectory("aicd_resources/jre/" + s);
				else
				{
					FileStream fs = new FileStream("aicd_resources/jre/" + s, FileMode.Create);
					byte[] buf = new byte[8192];
					int len;
					while ((len = zis.Read(buf, 0, buf.Length)) > 0)
					{
						fs.Write(buf, 0, len);
					}
					fs.Close();
				}
			}
			File.WriteAllText("aicd_resources/info.txt", "1");
		}
	}
}
