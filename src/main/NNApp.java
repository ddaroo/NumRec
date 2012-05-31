package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Arrays;
import javax.swing.*;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;


public class NNApp {
	
	public static File[] inputFiles;
	public static File inputDir;
	private static TrainingSet<SupervisedTrainingElement> trainingSet = new TrainingSet<SupervisedTrainingElement>(48,10);
	private static MultiLayerPerceptron network;
	private static int counter = 0;
	private static MainWnd mwnd;
	
	public static void main(String[] argv) {	
			loadDataFiles("samples");
			makeDataFile();
			preperDataSet();
			trainNetwork();
/*			for(SupervisedTrainingElement te:trainingSet.trainingElements())
				testNetwork(te);
			System.out.println("Skutecznoï¿½ï¿½ "+counter * (100/trainingSet.size()) +" %");
*/		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mwnd = new MainWnd();
			}
		});
	}
	
	public static void testNetwork(SupervisedTrainingElement input){
		network.setInput(input.getInput());
		double[] des = input.getDesiredOutput();
		network.calculate();
		double[] out = network.getOutput();
		int pos1 = 0,pos2 = 0;
		double max1 = out[0],max2 = des[0];
		for(int i = 0; i<out.length;i++) 
		{
			if(max1<out[i])
			{
				max1 = out[i];
				pos1 = i;
			}
			if(max2<des[i])
			{
				pos2 = i;
			}
		}
		if(pos1 == pos2) counter++;
		System.out.println("Wynik : "+Arrays.toString(out));
		System.out.println("Rozpoznano : " + pos1+ " dla "+ Arrays.toString(des));
	}
	
	public static int testNetwork(File f){
		int pos = 0;
		try
		{
			AudioSampleReader asd = new AudioSampleReader(f);
			int ch = asd.getFormat().getChannels();
			double[] samples = new double[(int)(asd.getSampleCount()/ch)];
			double[] interleaved = new double[(int)asd.getSampleCount()];
			asd.getInterleavedSamples(0, asd.getSampleCount(), interleaved);
	        asd.getChannelSamples(0, interleaved, samples);	        
	        double[] params = parametrProcessor.obliczParametry(samples);
	        network.setInput(params);
	        network.calculate();
	        
	        double[] out = network.getOutput();
			double max = out[0];
			for(int i = 0; i<out.length;i++) 
			{
				if(max<out[i])
				{
					max = out[i];
					pos = i;
				}
			}		
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		return pos;
	}
// Tworzenie sieci, ustawienie parametrów oraz trening
	public static void trainNetwork(){
		network = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 48,100,10);
		MomentumBackpropagation mb =(MomentumBackpropagation)network.getLearningRule();
		mb.setMomentum(0.25);
		mb.setLearningRate(0.1);
		network.randomizeWeights(-1, 1);
		mb.setMaxIterations(10000);
		network.learn(trainingSet);
	}
// Pobranie danych z pliku z zestawami ucz¹cymi	
	public static void preperDataSet(){
		try{
			trainingSet.clear();
			FileReader fr = new FileReader("samples/trainingData");
			BufferedReader br = new BufferedReader(fr);
			String s;
			String[] params;
			double [] coef = new double[58];
			while((s=br.readLine()) != null){
				params = s.split(" ");
				for(int i = 0; i<params.length;i++){
					coef[i] = Double.parseDouble(params[i]);
				}
				trainingSet.addElement(new SupervisedTrainingElement(Arrays.copyOfRange(coef, 0, 48), Arrays.copyOfRange(coef, 48, 58)));
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
		else System.out.println("Nie moï¿½na czytaï¿½");
	}
// Przetworzenie próbek i ich zapis to odpiwiedniego pliku	
	public static void makeDataFile(){
		try{
			FileWriter fw = new FileWriter("samples/trainingData");
			for(File f:inputFiles){
				String name = f.getName();
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
		        StringBuffer out = new StringBuffer("0 0 0 0 0 0 0 0 0 ");
		        String c = name.substring(0, 1);
		        out = out.insert((Integer.parseInt(c))*2,"1 ");
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

