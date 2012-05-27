package main;

import jAudioFeatureExtractor.AudioFeatures.*;



public class parametrProcessor {

	public static double[] obliczParametry(double[] probki){
		double[] parametry 	= new double[0];
		double[] norm 	= normal(probki);
		double[] filtrowanie 	= filtr(norm);
		double[] pociszy	= cisza(filtrowanie,1.0);
	
        //Obliczanie MEL CEPSTRUM dla kazdego okna, a potem wyliczanie sredniej	
		
												//ustalamy dĹ. okna na ok 10ms
			double[] probkiOkna = new double[400];
			double[][] mfcc = new double[2*(pociszy.length/400)][0];
			System.out.println("mel_cepstrum.length: "+mfcc.length);

			double[] okno = okno_Hamminga(400);
			for(int i=0;i<mfcc.length;i++){							//Dla kazdego okna(Nakladanie siÄ okien hamminga)
				for(int j=0;j<400;j++){
					probkiOkna[j]=pociszy[(400*(i/2))+j]*okno[j];	//Okienkowanie
				}
				mfcc[i] = obliczDlaOkna(probkiOkna);				//Obliczanie wsp. dla okna
			}
			double[] mfccSrednie = new double[mfcc[0].length-1];
			for(int i=0;i<mfcc[0].length-1;i++){			//dla wspolczynników cepstralnych od 1 do ostatniego (0. pomijamy)...
				mfccSrednie[i]=0;
				for(int j=0;j<mfcc.length;j++){			//sumuj po wszystkich oknach
					mfccSrednie[i]+=mfcc[j][i+1];
				}
				mfccSrednie[i]/=mfcc.length;	
				//Obliczenie sredniej.
			}
			System.out.print(mfcc[0].length);
			parametry=mfccSrednie;
			System.out.println();
			for (int i=0;i<56;i++)
				System.out.print(mfcc[i][1]+", ");
			System.out.println();
		System.out.println("Parametry obliczone dla pliku:");
		for(double p:parametry){
			System.out.print(p+", ");
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