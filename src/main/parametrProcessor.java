package main;

import jAudioFeatureExtractor.AudioFeatures.*;

public class parametrProcessor {

	public static double[] obliczParametry(double[] probki){
		double[] parametry 	= new double[0];
		double[] norm 	= normal(probki);
		double[] filtrowanie 	= filtr(norm);
		double[] pociszy = filtrowanie;//= cisza(filtrowanie,1.0);
	
        //Obliczanie MEL CEPSTRUM dla kazdego okna.											
			int wndSize = pociszy.length/2;
			double[] probkiOkna = new double[wndSize];
			double[][] mfcc = new double[2*(pociszy.length/wndSize)][0];			

			double[] okno = okno_Hamminga(wndSize);
			for(int i=0;i<mfcc.length;i++){							//Dla kazdego okna(Nakladanie siÃ„Â™ okien hamminga)
				for(int j=0;j<wndSize;j++){
					probkiOkna[j]=pociszy[(wndSize*(i/2))+j]*okno[j];	//Okienkowanie
				}
				mfcc[i] = obliczDlaOkna(probkiOkna);				//Obliczanie wsp. dla okna
			}
			parametry = new double[mfcc.length * (mfcc[0].length-1)];
			// Konkatenacja wspó³czynników z wszystkich okien
			for(int i = 0 ;i<mfcc.length;i++){
				System.arraycopy(mfcc[i], 1, parametry, 12*i, mfcc[i].length-1);
			}
		return parametry;

	}

	/**
	 * Oblicza odpowiednie parametry z zadanego ciagu probek
	 * @param m
	 * metoda obliczania parametrow
	 * @param probki
	 * ciag probek
	 * @return
	 * ciag obliczonych parametrow
	 */
	private static double[] obliczDlaOkna(double[] probki){
		double[] parametry = new double[0];

			MFCC mfcc = new MFCC();
			MagnitudeSpectrum spectrum = new MagnitudeSpectrum();
			try{
				int[] i =mfcc.getDepenedencyOffsets();
				double[] s=spectrum.extractFeature(probki, 44100, null);
				parametry = mfcc.extractFeature(probki, 44100, new double[][]{s});
			}catch(Exception e){
				e.printStackTrace();
			};
		

		return parametry;
	}

	/**
	 * Oblicza funkcja okna Hamminga
	 * @param dlugoscOkna
	 * Ilosc probek w oknie
	 * @return
	 * Tablica wartosci funkcji okna o zadanej dlugosci
	 */
	private static double[] okno_Hamminga(int dlugoscOkna){
		double[] okno = new double[dlugoscOkna];
		for(int i=0;i<dlugoscOkna;i++){
			okno[i]=0.54-0.46*(Math.cos((2.0*Math.PI*(double)i)/(dlugoscOkna)));
		}
		return okno;
	}

	private static double[] cisza(double[] probki,double prog){
		int p = 0;
		int k = probki.length-1;
		double s=0;
		for(int i=0;i<=probki.length-50;i+=50){
			s=0;
			for(int j=i;j<i+50;j++)
				s+=Math.pow(probki[j],2);
			if(s*100>prog){
				p=i;
				break;
			}
		}
		for(int i=probki.length-1-50;i>=0;i-=50){
			s=0;
			for(int j=i;j<i+50;j++)
				s+=Math.pow(probki[j],2);
			if(s*100>prog){
				k=i+50;
				break;
			}
		}

		double[] r=new double[k-p+1];
		int j=0;
		for(int i =p;i<=k;i++){
			r[j]=probki[i];
			j++;
		}
		return r;
	}

	/**
	 * Dokonuje normalizacji sygnalu do poziomu = 1
	 * @param probki
	 * sygnal wejsciowy
	 * @return
	 * sygnal wyjsciowy
	 */
	private static double[] normal(double[] probki){
		double zm=0;
		double[] r = new double[probki.length];
		for(double probka:probki){
			if(Math.abs(probka)>zm)
				zm = Math.abs(probka);
		}
		for(int i=1;i<r.length;i++){
			r[i] = (double)probki[i]/zm;
		}
		return r;
	}


	private static double[] filtr(double[] probki){
		double[] r = new double[probki.length];
		r[0]=probki[0];
		for(int i=1;i<r.length;i++){
			r[i]=probki[i]-0.95*probki[i-1];
		}
		return r;
	}



}