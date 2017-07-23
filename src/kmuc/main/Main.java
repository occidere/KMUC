package kmuc.main;

import kmuc.food.Carte;

public class Main {

	public static void main(String[] args) {
		//Carte carte = new Carte("2017-07-28");
		Carte carte = new Carte();
		System.out.println(carte.getCarteByTime());
	}
}