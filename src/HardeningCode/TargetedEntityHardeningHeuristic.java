package HardeningCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TargetedEntityHardeningHeuristic {
	private HashMap<String, Integer> entityLabeltoIndexMap; 
	private HashMap<String, List<List<String>>> IIRs;
	List<String> targetProtection;
	private int totalProtected;
	private int totalHardened = 0;
	
	TargetedEntityHardeningHeuristic(String file) throws FileNotFoundException{
		targetProtection = new ArrayList<String>();
		File caseFile = new File("OutFileForHeuristics/" + file + "Target.txt");
		Scanner scan = new Scanner(caseFile);
		targetProtection = Arrays.asList(scan.nextLine().split(" "));
		scan.close();
		entityLabeltoIndexMap = new HashMap<String, Integer>();
		IIRs = new HashMap<String, List<List<String>>>();
		caseFile = new File("OutFileForHeuristics/" + file + ".txt");
		scan = new Scanner(caseFile);
		int eIndex = 0;
		while(scan.hasNext()){
			String exp = scan.nextLine();
			StringBuilder firstEntity = new StringBuilder();
			int index = 0;
			while(exp.charAt(index) != ' '){
				firstEntity.append(exp.charAt(index));
				index ++;
			}
			if(!entityLabeltoIndexMap.containsKey(firstEntity.toString())){
				entityLabeltoIndexMap.put(firstEntity.toString(), eIndex);
				eIndex ++;
			}
			index ++;
			while(exp.charAt(index) != ' '){
				index ++;
			}
			String[] minterms = exp.substring(index + 1, exp.length()).split("   ");
			List<List<String>> dependency = new ArrayList<List<String>>();
			for(String str: minterms){
				dependency.add(Arrays.asList(str.split(" ")));
				for(String entity: str.split(" ")){
					if(!entityLabeltoIndexMap.containsKey(entity)){
						entityLabeltoIndexMap.put(entity, eIndex);
						eIndex ++;
					}
				}
			}
			IIRs.put(firstEntity.toString(), dependency);
		}
		scan.close();
	}
	
	public void compute(){
		int targetProtect = 0;
		List<String> protectedEntities = new ArrayList<String>();
		while(targetProtect != targetProtection.size()){
			totalHardened ++;
			HashMap<String, List<List<String>>> IIRsForIteration = new HashMap<String, List<List<String>>>();
			List<String> protectSetForIteration = new ArrayList<String>();
			double mintermHitNum = 0;
			int countTarget = 0;
			for(String entity: entityLabeltoIndexMap.keySet()){
				if(protectedEntities.contains(entity)) continue;
				
				HashMap<String, List<List<String>>> IIRdummy = new HashMap<String, List<List<String>>>();
				List<String> curFailedEntity = new ArrayList<String>(); 
				curFailedEntity.add(entity);
				double curMintermHitNum = 0;
				int curCountTarget = 0;
				if(targetProtection.contains(entity)) curCountTarget ++;
				
				for(String str: IIRs.keySet())
					if(!protectedEntities.contains(str))
						IIRdummy.put(str, IIRs.get(str));
				int start = 0;
				while(start < curFailedEntity.size()){
					String entityProtected = curFailedEntity.get(start);
					List<String> termsToIterate = new ArrayList<String>();
					for(String str: IIRdummy.keySet()) termsToIterate.add(str);
					for(String firstTerm: termsToIterate){
						List<List<String>> mintermToAdd = new ArrayList<List<String>>();
						for(List<String> minterm: IIRdummy.get(firstTerm)){
							List<String> minToAdd = new ArrayList<String>();
							double totalUnCov = 0;
							for(String str: minterm){
								if(!str.equals(entityProtected)){
									minToAdd.add(str);
									totalUnCov ++;
								}
							}
							if(targetProtection.contains(firstTerm))
									curMintermHitNum += (double) (minterm.size() - totalUnCov) / (double) minterm.size();
							if(minToAdd.size() != 0) mintermToAdd.add(minToAdd);
						}
						IIRdummy.replace(firstTerm, mintermToAdd);
					}
					for(String str: termsToIterate){
						if(IIRdummy.get(str).size() != IIRs.get(str).size()){
							IIRdummy.remove(str);
							if(!curFailedEntity.contains(str)){
								curFailedEntity.add(str);
								if(targetProtection.contains(str)) curCountTarget ++;
							}
						}
					}
					start ++;
				}
				if(curCountTarget > countTarget){
					countTarget = curCountTarget;
					protectSetForIteration = new ArrayList<String>();
					IIRsForIteration = new HashMap<String, List<List<String>>>();
					for(String str: curFailedEntity)
						protectSetForIteration.add(str);
					for(String str: IIRdummy.keySet())
						IIRsForIteration.put(str,  IIRdummy.get(str));
				}
				
				else if(curCountTarget == countTarget){
					if(mintermHitNum < curMintermHitNum){
						mintermHitNum = curMintermHitNum;
						protectSetForIteration = new ArrayList<String>();
						IIRsForIteration = new HashMap<String, List<List<String>>>();
						for(String str: curFailedEntity)
							protectSetForIteration.add(str);
						for(String str: IIRdummy.keySet())
							IIRsForIteration.put(str,  IIRdummy.get(str));
					}
				}
				
			}
			targetProtect += countTarget;
			for(String str: protectSetForIteration)
				protectedEntities.add(str);

			IIRs = new HashMap<String, List<List<String>>>();
			for(String str: IIRsForIteration.keySet()){
				IIRs.put(str, IIRsForIteration.get(str));
			}
			
		}
		totalProtected = protectedEntities.size();
	}
	
	public int getTotalProtected() { return totalProtected;}
	public int getTotalHardened() { return totalHardened;}
	
	public static void main(String[] args) throws FileNotFoundException{
		TargetedEntityHardeningHeuristic Object = new TargetedEntityHardeningHeuristic("case9IIRsAtTimeStep1");
		Object.compute();
		System.out.println("Total Protected: " +  Object.getTotalProtected() + " Total Hardened: " + Object.getTotalHardened());
	}
}
