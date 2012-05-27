package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Arrays;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.util.TransferFunctionType;


public class NNApp {
	public static File[] inputFiles;
	public static File inputDir;
	private static TrainingSet<SupervisedTrainingElement> trainingSet = new TrainingSet<SupervisedTrainingElement>(12,10);
	private static MultiLayerPerceptron network;
	
	public static void main(String[] argv) {
		loadDataFiles("samples");
		makeDataFile();
		preperDataSet();
		trainNetwork();
		for(TrainingElement te:trainingSet.trainingElements())
			testNetwork(te);  
	}
	
	public static void testNetwork(TrainingElement input){
		network.setInput(input.getInput());
		network.calculate();
		double[] out = network.getOutput();
		int pos = 0;
		double max = out[0];
		for(int i = 0; i<out.length;i++)
			if(max<out[i])
			{
				max = out[i];
				pos = i;
			}
		System.out.println("Wynik : "+Arrays.toString(out));
		System.out.println("Rozpoznano : " + pos);
	}
	
	public static void trainNetwork(){
		network = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 12,10,10);
		network.learn(trainingSet);
	}
	
	public static void preperDataSet(){
		try{
			trainingSet.clear();
			FileReader fr = new FileReader("samples/trainingData");
			BufferedReader br = new BufferedReader(fr);
			String s;
			String[] params;
			double [] coef = new double[22];
			while((s=br.readLine()) != null){
				params = s.split(" ");
				for(int i = 0; i<params.length;i++){
					coef[i] = Double.parseDouble(params[i]);
				}
				trainingSet.addElement(new SupervisedTrainingElement(Arrays.copyOfRange(coef, 0, 12), Arrays.copyOfRange(coef, 12, 22)));
			}
			fr.close();
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public static void loadDataFiles(String dirPath){
		inputDir = new File(dirPath);
		if(inputDir.canRead())
			inputFiles = inputDir.listFiles(new waveFilter());
		
		else System.out.println("Nie mo¿na czytaæ");
	}
	
	public static void makeDataFile(){
		try{
			FileWriter fw = new FileWriter("samples/trainingData");
			for(File f:inputFiles){
				String name = f.getName();
				System.out.println(name);
				AudioSampleReader asd = new AudioSampleReader(f);
				int ch = asd.getFormat().getChannels();
				double[] samples = new double[(int)(asd.getSampleCount()/ch)];
				double[] interleaved = new double[(int)asd.getSampleCount()];
				asd.getInterleavedSamples(0, asd.getSampleCount(), interleaved);
		        asd.getChannelSamples(0, interleaved, samples);	        
		        double[] params = parametrProcessor.obliczParametry(samples);
		        String sParams = "";
		        for(double p:params)
		        	sParams += String.valueOf(p)+" ";
		        StringBuffer out = new StringBuffer("0 0 0 0 0 0 0 0 0");
		        String c = name.substring(0, 1);
		        out = out.insert((Integer.parseInt(c))*2,"1 ");
		        System.out.println(sParams);
		        fw.write(sParams+out+"\n");
			}
			fw.close();
		}
		catch (IOException e) {
			System.out.print("Something wrong: "+e.getMessage());		
		} 
		catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
}


class waveFilter implements FilenameFilter{
	@Override
	public boolean accept(File dir, String name){
		return name.endsWith(".wav");
	}
}

