package com.hbo.interview.bowling;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TenPinBowlingScorer is an implementation of Scorer for ten-pin bowling game.
 * @author Weige
 *
 */
public class TenPinBowlingScorer implements Scorer{
	static final Logger LOG = LoggerFactory.getLogger(TenPinBowlingScorer.class);
	
	//Total number of frames in each ten bin bowling game
	private final static int NUMBER_OF_FRAMES=10;
	private final static int LAST_FRAME_NUMBER=NUMBER_OF_FRAMES;
	
	//A list contains all players in the same order as the input stream
	private List<String> players = new ArrayList<>();
	
	//Stores total score for each player
	private Map<String,Integer> totalScores = new HashMap<>();
	
	//Stores scores of each frames for each players
	//It maps each player's name to first frame score, the subsequence frame scores
	//can be found by TenPinBowlingScore's nextFrame method.
	private Map<String,TenPinBowlingScore> scores = new HashMap<String,TenPinBowlingScore>();
	
	//Indicates whether this Scorer has been initialized.
	private volatile boolean isInitialized = false;
	
	private int numOfPlayers;
	
    private void calculateTotalScore() {
		for(String player:players){
			TenPinBowlingScore score= scores.get(player);
			int totalScore = 0;
			//Iterate through all frames, and calculate the total scores
			while(score !=null){
				totalScore+=score.calculateScore();
				score=score.getNextFrame();
			}
			totalScores.put(player, totalScore);
		}
		
	}
    
	//Loading scores for each player
    private void initializeScores(Scanner scoresInputScanner) {
    
    	//Contains previous frame score for each player
    	Map<String,TenPinBowlingScore> currentFrameScore = new HashMap<String,TenPinBowlingScore>();
    	//We assume there are fixed number of frames in each game and each player has a score for each frame
    	//following nested loops iterate through every players for each frame, and read and parse the score
    	//if EOF is reached before end of loop, we know the input must be invalid because missing scores
		for(int frame=1;frame<=NUMBER_OF_FRAMES;frame++){
			for(int playerIndex =0;playerIndex<players.size();playerIndex++){
				if(!scoresInputScanner.hasNext()){
	    			throw new InvalidInput("Incomplete input: missing scores");
	    		}else{
	    			TenPinBowlingScore score = new TenPinBowlingScore(frame,scoresInputScanner.nextLine());
	    			String player=players.get(playerIndex);
	    			if(frame ==1){
	    				this.scores.put(player, score);
	    				currentFrameScore.put(player, score);
	    			}else{
	    				currentFrameScore.get(player).setNextFrame(score);
	    				currentFrameScore.put(player, score);
	    			}
	    		}
			}
		}
		
	}
    
	private void initializePlayerList(Scanner scoresInputScanner){
    	try{
    		//First line of the input must be the number of players
    		numOfPlayers = Integer.parseInt(scoresInputScanner.nextLine());
    	}catch(NoSuchElementException e){
    		//If the input is empty, it would throws a NoSuchElementException.
    		throw new InvalidInput("Input connot be empty.");
    	}catch(NumberFormatException e){
    		//If first input is not a number
    		throw new InvalidInput("Input must starts with an integer value.");
    	}
    	
    	if(numOfPlayers<=0){
    		throw new InvalidInput("The number of players must be greater than zero.");
    	}
    	//A set is used to check if there are duplicates
    	Set<String> names=new HashSet<>();
    	for(int i=0;i<this.numOfPlayers;i++){
    		if(!scoresInputScanner.hasNext()){
    			throw new InvalidInput(String.format("Incomplete input: there are only %d of %d payers", i,numOfPlayers));
    		}else{
    			String name = validPlayerName(scoresInputScanner.nextLine());
    			if(names.contains(name)){
    				throw new InvalidInput("Duplicate player name: "+name);
    			}
    			names.add(name);
    			players.add(name);
    		}
    	}
    }
	//Method for trimming leading and trailing spaces, and it also valid player's name
    private String validPlayerName(String name) {
		name=name.trim();
		if(name.length()==0){
			throw new InvalidInput("Player's name cannot be empty");
		}
		return name;
	}
    /**
     * @param inputStream the text stream containing the input
     * @throws InvalidInput if {@code inputStream} does not conform to the format listed above or is null.
     */
    @Override 
    public void init(InputStream inputStream) throws InvalidInput{
    	LOG.debug("initializing ten-pin bowling scorer: {}",inputStream+"");
    	if(inputStream ==null)
    		throw new InvalidInput("input stream cannot be null!");
    	try{
	    	Scanner sc=new Scanner(inputStream);
	    	
	    	initializePlayerList(sc);
	    	LOG.debug("player list initialization finished");
	    	initializeScores(sc);
	    	LOG.debug("player scores initialization finished");
	    	if(sc.ioException()!=null){
	    		LOG.error("Error on processing input file",sc.ioException());
	    		throw new InvalidInput("input stream error");
	    	}
	    	
	    	calculateTotalScore();
	    	LOG.debug("total score calculation finished");
	    	isInitialized=true;
    	}finally{
    		try {
    			if(inputStream!=null)
    				inputStream.close();
			} catch (IOException e) { 
				LOG.error("failed to close input stream",e);
				throw new InvalidInput("failed to close input stream");
			}
    	}
    	 
    	 
    } 
	/**
     * Assumes {@link #init(java.io.InputStream)} has already been called.  It is undefined what would happen if
     * this method was called before {@linkplain #init(java.io.InputStream)}
     * @return the list of players parsed from the {@linkplain #init(java.io.InputStream)} in order
     */
    @Override 
    public List<String> getPlayers(){
    	if(!this.isInitialized)
    		return null;
    	LOG.debug("returns a player list: {}",this.players);
    	return new ArrayList<>(this.players);
    }

    /**
     * Assumes {@link #init(java.io.InputStream)} has already been called.  It is undefined what would happen if
     * this method was called before {@linkplain #init(java.io.InputStream)}
     * @param player for which to retrieve his/her score
     * @return the final score for {@code player} 
     * @throws InvalidInput if {@code player} is null or not in the list of known players
     */
    @Override 
    public Integer getPlayerScore(String player) throws InvalidInput{
    	if(!this.isInitialized){
    		return -1;
    	}
    	LOG.info("get score for player: {}",player);
    	if(player==null || !this.totalScores.containsKey(player)){
    		throw new InvalidInput(String.format("Player %s is not in the game!", player));
    	}
    	return this.totalScores.get(player);
    }
    
    //Class represents the score for a specified frame.
    //It also validates the scores are valid or not
    static class TenPinBowlingScore {
    	public final String SCORES_STRING_MATCHER = "\\s*((10|\\d)\\s*){1,3}"; 
    	private enum ScoreType{
    		STRIKE,SPARE,OPEN_FRAME;
    	}
    	//Represents the frame number in which 
    	private int frame;
    	//An int array of size 3 since there are at most 3 scores for each frame
    	private int[] scores = new int[3];
    	
    	//Score for next frame
    	private TenPinBowlingScore nextFrame;
    	
    	private ScoreType type;
    	public TenPinBowlingScore(int frame,String scoresStr){
    		LOG.trace("creating score for frame {} from \"{}\"",frame,scoresStr);
    		//Validate input 
    		//Check if frame is in the range of 1 to 10
    		if(frame>LAST_FRAME_NUMBER || frame <1){
    			throw new InvalidInput("Invalid frame!");
    		}
    		//Check if scoresStr contains valid scores format,
    		//Since there are at most three scores in each frame, the regex 
    		//matches only a string that contains at least one positive number, or at most three positive numbers 
    		//between 0 and 10 separated by spaces.
    		if(scoresStr==null || !scoresStr.matches(SCORES_STRING_MATCHER)) {
    			throw new InvalidInput("Invalid score: "+scoresStr);
    		}
    		this.frame = frame;
    		//Tokenize the scores string and parse them into integers 
    		//since already validated in previous step, it is certain that
    		//scoresStr must contains only number strings. 
    		String scoreTokens[] = scoresStr.trim().split("\\s+");
    		for(int i=0;i<scoreTokens.length;i++){
    			scores[i] = Integer.parseInt(scoreTokens[i]);
    		} 
    		validateScores(scoreTokens.length);
    		
    		LOG.trace("constructed score successfully: {}",this.type);
    	}
    	private void validateScores(int scoreTokensSize){
    		//Regex matching above already make sure that each number is in range of 0 and 10
    		//We don't need to validate the value range again here.
    		if(frame == LAST_FRAME_NUMBER){
    			if(scores[0]==10 && scoreTokensSize==3){
    				type = ScoreType.STRIKE;
    			}else if(scores[0]!=10 && scores[0]+scores[1] ==10 && scoreTokensSize==3){
    				type = ScoreType.SPARE;
    			}else if(scores[0]+scores[1]<10 && scoreTokensSize==2){
    				type = ScoreType.OPEN_FRAME;
    			}else{
    				throw new InvalidInput("Invalid scores");
    			}
    		}else{
    			if(scores[0]==10 && scoreTokensSize==1 ){
    				type = ScoreType.STRIKE;
    			}else if(scores[0]!=10 && scores[0]+scores[1] ==10 && scoreTokensSize==2){
    				type = ScoreType.SPARE;
    			}else if(scores[0]+scores[1]<10 && scoreTokensSize==2){
    				type = ScoreType.OPEN_FRAME;
    			}else{
    				throw new InvalidInput("Invalid scores");
    			}
    		}
    		
    	}
		public TenPinBowlingScore getNextFrame() {
			return nextFrame;
		}
		public void setNextFrame(TenPinBowlingScore nextFrame) {
			this.nextFrame = nextFrame;
		} 
		//Calculate current score
		public int calculateScore(){
			LOG.debug("calculating score: {}",this.toString());
			switch(this.type){
			case STRIKE:
				if(frame == LAST_FRAME_NUMBER){
					return scores[0]+scores[1]+scores[2];
				}else{
					return scores[0] + this.nextFrame.scores[0]+(
							(this.nextFrame.type==ScoreType.STRIKE && this.nextFrame.frame!= LAST_FRAME_NUMBER)?this.nextFrame.nextFrame.scores[0]:this.nextFrame.scores[1]);
				}
			case SPARE:
				if(frame == LAST_FRAME_NUMBER){
					return scores[0]+scores[1]+scores[2];
				}else{
					return scores[0]+scores[1] + this.nextFrame.scores[0];
				}
			case OPEN_FRAME: 
				return scores[0]+scores[1];
			
			}
			throw new RuntimeException("SHALL NOT BE HERE!");
		}
		
		@Override 
		public String toString(){
			return String.format("type: %s; scores: %s", this.type,Arrays.toString(this.scores));
		}
    }

    
    
}
