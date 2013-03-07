package it.sardegnaricerche.voiceid.mapred;

import it.sardegnaricerche.voiceid.sr.VCluster;
import it.sardegnaricerche.voiceid.sr.Voiceid;
import it.sardegnaricerche.voiceid.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.json.JSONObject;

	public class DiarizationMapper extends
			Mapper<LongWritable, Text, Text,  Text> {
		
		public static Logger l = Logger.getLogger("TaskMapper");
		
		 public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			 	ArrayList<VCluster> list_clusters = new ArrayList<VCluster>();
				try {
					l.info("value: "+value);
					Configuration conf = new Configuration();
					FileSystem fs = FileSystem.get(conf);
					Path inFile = new Path("/home/hduser/output");
					if (!fs.exists(inFile))
						l.info(inFile+" exist");
					
					Voiceid v = new Voiceid("/home/hduser/.voiceid/gmm_db/", value.toString());
					v.extractClusters();
					
					File f = new File(value.toString());
					JSONObject obj = v.toJson();
					//FileWriter fstream = new FileWriter(f.getAbsolutePath().replaceFirst("[.][^.]+$", "") + ".json");
					FileWriter fstream = new FileWriter(Utils.getBasename(f) + ".json");
					BufferedWriter out = new BufferedWriter(fstream);
					out.write(obj.toString());
					//Close the output stream
					out.close();
					list_clusters = v.getClusters();
					
				} catch (Exception e) {
					throw new IOException(e);
				}
				for (VCluster c : list_clusters){
					try {
						context.write(new Text(""), new Text(c.getSample().getResource().getAbsolutePath()));
					} catch (UnsupportedAudioFileException e) {
						// TODO Auto-generated catch block
						l.severe(e.getMessage());
					}
				}
	        }

//		public void map(Text key, Text value, Context context)
//				throws IOException, InterruptedException {
////		Shell.execCommand("vid -h");
//		context.write(key, value);//(new Text("key"), new Text("value"));
//		}
	}