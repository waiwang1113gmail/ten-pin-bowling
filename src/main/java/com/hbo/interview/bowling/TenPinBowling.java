package com.hbo.interview.bowling;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hbo.interview.bowling.Scorer.InvalidInput;

/**
 * @author Weige
 *
 */
public class TenPinBowling {
	static final Logger LOG = LoggerFactory.getLogger(TenPinBowling.class);
	public static void main(String args[]){
		if(args.length==0){
			System.err.println("ERROR: no input file provided");
			System.err.println("usage: java TenPinBowling infile");
			System.err.println("\tinfile: the input file contains players' names and scores");
		}
		String inputFile =args[0];
		LOG.info("Using input file: {}",inputFile);
		
		TenPinBowlingScorer scorer = new TenPinBowlingScorer();
		try {
			scorer.init(new FileInputStream(inputFile));
		} catch (InvalidInput e) {
			LOG.error("failed to parse scores",e);
			System.err.println("ERROR: Failed to parse scores");
			System.exit(1);
		} catch (FileNotFoundException e) {
			LOG.error("file not found: "+inputFile,e);
			System.err.format("ERROR: %s is not found!\n",inputFile);
			System.exit(1);
		}
		for(String player:scorer.getPlayers()){
			System.out.format("Player %s scored %d\n",player,scorer.getPlayerScore(player));
		}
	}
}
