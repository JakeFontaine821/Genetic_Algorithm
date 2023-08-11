package GA_Package;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Random;

import java.lang.Math;

public class MyPanel extends JPanel implements ActionListener{
	static final int SCREEN_WIDTH = 1300;
	static final int SCREEN_HEIGHT = 750;
	static final int DELAY = 200;
	boolean running = false;
	Timer timer;
	Random random;	
	
	static final int GA_POPSIZE = 30;
	static final int GA_MAXITER = 16000;
	static final float GA_ELITRATE = .1f;
	static final float GA_MUTATIONRATE = .001f;
	static final int GA_TARGETLOC = 1000;
	static final int GRAVITY = 10;
	static final int GA_MAXVALUE = 500;
	
	ArrayList<ga_struct> population = new ArrayList<ga_struct>();
	ArrayList<ga_struct> buffer = new ArrayList<ga_struct>();
	ga_struct drawnToScreen = new ga_struct();
	int generation = 1;	
	boolean run_GA = true;	
	
	MyPanel(){
		random = new Random();
		this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
		this.setBackground(Color.CYAN);
		this.setFocusable(true);
		this.addKeyListener(new MyKeyAdapter());
		
		startGame();
	}
	
	public void startGame() {
		running = true;
		timer = new Timer(DELAY,this);
		timer.start();		
		
		init_population(population);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}
	
	public void draw(Graphics g) {		
		if(running) {
			Graphics2D g2D = (Graphics2D) g;
			
			/****************************************************************************************/
			//INSTRUCTIONS DRAWN
			g2D.setColor(Color.black);
			g2D.setFont(new Font("Arial", Font.BOLD, 25));
			g2D.drawString("ESC to Exit | SPACE to start over", 0, 20);
			
			/****************************************************************************************/
			//DRAW THE CONSTANT GROUND AND TARGET THAT DO NOT MOVE, ALSO GENERATION COUNTER						
			g2D.drawString("Generation: " + Integer.toString(generation), (SCREEN_WIDTH / 2) - 100, 50);
			
			g2D.setColor(Color.green);
			g2D.fillRect(0, SCREEN_HEIGHT - 100, SCREEN_WIDTH, 100);
			
			g2D.setColor(Color.red);
			g2D.fillOval(GA_TARGETLOC - 40, SCREEN_HEIGHT - 100, 80, 20);
			g2D.setColor(Color.white);
			g2D.fillOval(GA_TARGETLOC - 30, SCREEN_HEIGHT - 100, 60, 15);
			g2D.setColor(Color.red);
			g2D.fillOval(GA_TARGETLOC - 20, SCREEN_HEIGHT - 100, 40, 10);			
			
			/****************************************************************************************/
			// DRAW CLIFF, CANNON, AND CANNON BALL ALL BASED ON BEST CHROMO IN POPULATION
			
			g2D.setColor(Color.red);
			g2D.setStroke(new BasicStroke(3));
			g2D.drawArc(/*START X POS*/-drawnToScreen.distance, /*START Y POS*/SCREEN_HEIGHT - (drawnToScreen.height + 110), 
					/*WIDTH OF ARC*/drawnToScreen.distance * 2, /*HEIGHT OF ARC*/drawnToScreen.height * 2 + 10, 
					/*START ANGLE AROUND CIRCLE*/0, /*END ANGLE AROUND CIRCLE*/85);
			
			g2D.setColor(Color.green);
			g2D.fillRect(0, SCREEN_HEIGHT - (drawnToScreen.height + 100), 100, 500);
			g2D.setColor(Color.black);
			g2D.fillRect(50, SCREEN_HEIGHT - (drawnToScreen.height + 120), 50, 25);
			
			g2D.fillOval(drawnToScreen.distance - 5, SCREEN_HEIGHT - 105, 10, 10);			
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if(running) {
			//UPDATE FUNCTION RIGHT HERE
			if(run_GA) {
				calc_fitness(population);
				sort_by_fitness(population);
				print_best(population);
				
				if(Math.abs(population.get(0).fitval) < 10) { 
					run_GA = false; 
				}
				else {
					mate(population, buffer);
					swap(population, buffer);
					
					generation++;
				}
			}			
		}
		repaint();
	}
	
	public class MyKeyAdapter extends KeyAdapter{
		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {			
				case KeyEvent.VK_ESCAPE:
					timer.stop();
					System.exit(0);
					break;
					
				//other stuff
				case KeyEvent.VK_SPACE:
					init_population(population);
					break;
			}
		}
	}
	
	public void init_population(ArrayList<ga_struct> population) {
		
		population.clear();
		generation = 1;
		run_GA = true;
		
		for(int i = 0; i < GA_POPSIZE; i++) {
			ga_struct newChromo = new ga_struct();
			
			newChromo.fitval = 0;
			newChromo.height = random.nextInt(0, GA_MAXVALUE);
			newChromo.vel = random.nextInt(0, GA_MAXVALUE);
			
			population.add(newChromo);
		}
	}
	
	public void calc_fitness(ArrayList<ga_struct> population) {
		
		for(int i = 0; i < GA_POPSIZE; i++) {
			ga_struct chromo = population.get(i);
			
			double time = Math.sqrt((2 * chromo.height) / GRAVITY);
			chromo.distance = (int) (time * chromo.vel);
			chromo.fitval = GA_TARGETLOC - chromo.distance;
			
			population.set(i, chromo);
		}		
	}
	
	public void sort_by_fitness(ArrayList<ga_struct> population) {
		for(int i = 0; i < GA_POPSIZE - 1; i++) {
			
			for(int j = 0; j < GA_POPSIZE - 1; j++) {
				
				if(Math.abs(population.get(j).fitval) > Math.abs(population.get(j + 1).fitval)) {
					
					ga_struct temp = population.get(j);
					population.set(j, population.get(j + 1));
					population.set(j + 1, temp);
				}
			}
		}
	}
	
	public void print_best(ArrayList<ga_struct> population) {
		drawnToScreen = population.get(0);
		
		System.out.println(drawnToScreen.fitval);
	}
	
	public void elitism(ArrayList<ga_struct> population, ArrayList<ga_struct> buffer) {
		buffer.clear();
		
		buffer.add(population.get(0));
		buffer.add(population.get(1));
	}
	
	public void mate(ArrayList<ga_struct> population, ArrayList<ga_struct> buffer) {
		/***************************************************************/
		//ELITISM
		elitism(population, buffer);
		
		/***************************************************************/
		// MAKE CHILDREN		
		for(int i = 0; i < GA_POPSIZE - 2; i += 2) {
			int i1 = random.nextInt(2, GA_POPSIZE);
			int i2 = random.nextInt(2, GA_POPSIZE);
			
			ga_struct child1 = new ga_struct();
			
			child1.height = population.get(i1).height;
			child1.vel = population.get(i2).vel;
			child1.fitval = 0;
			
			buffer.add(child1);
			
			ga_struct child2 = new ga_struct();
			
			child2.height = population.get(i2).height;
			child2.vel = population.get(i1).vel;
			child2.fitval = 0;
			
			buffer.add(child2);
		}
		
		/**************************************************************/
		// MUTATE
		
		for(int i = 0; i < GA_POPSIZE; i++) {
			int mutate = random.nextInt(1, 100);
			
			if(mutate == 1) {
				mutate(buffer.get(i));
			}
		}
	}
	
	public void mutate(ga_struct member) {
		member.height = random.nextInt(0, GA_MAXVALUE);
		member.vel = random.nextInt(0, GA_MAXVALUE);
	}
	
	public void swap(ArrayList<ga_struct> population, ArrayList<ga_struct> buffer) {
		population.clear();
		
		for(int i = 0; i < GA_POPSIZE; i++) {
			population.add(buffer.get(i));
		}
	}
}
