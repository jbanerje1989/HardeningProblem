package HardeningCode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class EntityHardeningHeuristic {
	private HashMap<String, Integer> entityLabeltoIndexMap; 
	private HashMap<String, List<List<String>>> IIRs;
	private int totalProtected;
	private int hardeningBudget = 0;
	
	EntityHardeningHeuristic(String file, int hardVal) throws FileNotFoundException{
		hardeningBudget = hardVal;
		entityLabeltoIndexMap = new HashMap<String, Integer>();
		IIRs = new HashMap<String, List<List<String>>>();
		File caseFile = new File("OutFileForHeuristics/" + file + ".txt");
		Scanner scan = new Scanner(caseFile);
		int eIndex = 0;
		for(String str: scan.nextLine().split(" ")){
			entityLabeltoIndexMap.put(str, eIndex);
			eIndex ++;
		}
		while(scan.hasNext()){
			String exp = scan.nextLine();
			StringBuilder firstEntity = new StringBuilder();
			int index = 0;
			while(exp.charAt(index) != ' '){
				firstEntity.append(exp.charAt(index));
				index ++;
			}
			index ++;
			while(exp.charAt(index) != ' '){
				index ++;
			}
			String[] minterms = exp.substring(index + 1, exp.length()).split("   ");
			List<List<String>> dependency = new ArrayList<List<String>>();
			for(String str: minterms)
				dependency.add(Arrays.asList(str.split(" ")));
			IIRs.put(firstEntity.toString(), dependency);
		}
		scan.close();
	}
	
	public void compute(){
		int hardening = 0;
		List<String> protectedEntities = new ArrayList<String>();
		while(hardening < hardeningBudget){
			hardening ++;
			HashMap<String, List<List<String>>> IIRsForIteration = new HashMap<String, List<List<String>>>();
			List<String> protectSetForIteration = new ArrayList<String>();
			double mintermHitNum = 0;
			for(String entity: entityLabeltoIndexMap.keySet()){
				if(protectedEntities.contains(entity)) continue;
				
				HashMap<String, List<List<String>>> IIRdummy = new HashMap<String, List<List<String>>>();
				List<String> curFailedEntity = new ArrayList<String>(); 
				curFailedEntity.add(entity);
				double curMintermHitNum = 0;
				
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
							curMintermHitNum += (double) (minterm.size() - totalUnCov) / (double) minterm.size();
							if(minToAdd.size() != 0) mintermToAdd.add(minToAdd);
						}
						IIRdummy.replace(firstTerm, mintermToAdd);
					}
					for(String str: termsToIterate){
						if(IIRdummy.get(str).size() != IIRs.get(str).size()){
							IIRdummy.remove(str);
							if(!curFailedEntity.contains(str)) curFailedEntity.add(str);
						}
					}
					start ++;
				}
				if(curFailedEntity.size() > protectSetForIteration.size()){
					protectSetForIteration = new ArrayList<String>();
					IIRsForIteration = new HashMap<String, List<List<String>>>();
					for(String str: curFailedEntity)
						protectSetForIteration.add(str);
					for(String str: IIRdummy.keySet())
						IIRsForIteration.put(str,  IIRdummy.get(str));
				}
				
				else if(curFailedEntity.size() == protectSetForIteration.size()){
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
			
			for(String str: protectSetForIteration)
				protectedEntities.add(str);

			IIRs = new HashMap<String, List<List<String>>>();
			for(String str: IIRsForIteration.keySet())
				IIRs.put(str, IIRsForIteration.get(str));
		}
		totalProtected = protectedEntities.size();
		System.out.println("Protected Entities");
		Collections.sort(protectedEntities);
		System.out.println(protectedEntities);
	}
	
	public int getTotalProtected() { return totalProtected;}
	
	public static void main(String[] args) throws FileNotFoundException{
		EntityHardeningHeuristic Object = new EntityHardeningHeuristic("case30IIRsAtTimeStep1", 4);
		Object.compute();
		System.out.println("Total Protected: " +  Object.getTotalProtected());
	}
}
