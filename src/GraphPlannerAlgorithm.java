import java.io.*;
import java.util.*;

// GraphLayer class is the base class which represents each layer of graph plan 
class Graphlayer {

	public ArrayList<String> preconditionList;
	public ArrayList<String> effectList;
	public String actionName;

	public Graphlayer() {
		preconditionList = new ArrayList<>();
		effectList = new ArrayList<>();
	}

	public String getName() {
		return actionName;
	}

	public void setName(String name) {
		this.actionName = name;
	}

}

public class GraphPlannerAlgorithm {
	
	static HashMap<Integer, HashSet<String>> finalStates;
	static HashMap<Integer, HashSet<String>> finalActions;
	static ArrayList<ArrayList> allMutex;
	static ArrayList<HashSet<Graphlayer>> allActions;
	static HashSet<Graphlayer> graphActions;
	static ArrayList<HashSet<String>> graphlayersStates;
	static HashSet<String> states;
	static HashSet<String> planerStates;
	static HashSet<Graphlayer> actionsPath;
	static ArrayList<String> goalStates;
	static ArrayList<ArrayList<ArrayList>> interferenceMutexList; 
	static ArrayList<ArrayList<ArrayList>> negatedLiteralsMutexList;
	static ArrayList<ArrayList<ArrayList>> inconsistentEffectsMutexList;
	static ArrayList solutionPath;
	
	GraphPlannerAlgorithm() {
		allActions = new ArrayList<>();
		allMutex = new ArrayList<>();
		graphlayersStates = new ArrayList<>();
		graphActions = new HashSet<Graphlayer>();
		planerStates = new HashSet<>();
		states = new HashSet<String>();
		goalStates = new ArrayList<>();
		actionsPath = new HashSet<>();	
		finalStates = new HashMap<Integer, HashSet<String>>();
		finalActions = new HashMap<Integer, HashSet<String>>();
		interferenceMutexList = new ArrayList<>();
		negatedLiteralsMutexList = new ArrayList<>();
		inconsistentEffectsMutexList = new ArrayList<>();
		solutionPath = new ArrayList<>();
	}
	
	public static void main(String[] args) {
		
		GraphPlannerAlgorithm graphAlgo = new GraphPlannerAlgorithm();
		
		try {
			graphAlgo.parseInputFile(args[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		negatedLiteralsMutexList.add(null);
		
		// We will check level off condition otherwise we will expand
		while (!graphAlgo.checkLevelOffCondition(goalStates, graphlayersStates.get(graphlayersStates.size() - 1))) {
			graphAlgo.findNextLayerActions(graphActions, graphlayersStates, allActions, actionsPath);
		}
		
		graphAlgo.findNextLayerActions(graphActions, graphlayersStates, allActions, actionsPath);
		graphAlgo.findNextLayerActions(graphActions, graphlayersStates, allActions, actionsPath);
		finalStates.put(graphlayersStates.size() - 1, graphlayersStates.get(graphlayersStates.size() - 1));		
		// method to print all details
		graphAlgo.printDetails();

	}

	private void printDetails() {
		System.out.println("Initial States: "+planerStates);
		System.out.println("Goal States: "+goalStates);
		System.out.println();
		
		// Printing all layer wise states
		System.out.println("States Layer wise:");
				for (Integer i : finalStates.keySet()) {
			System.out.println("Layer "+ i + ":" + finalStates.get(i));
		}

		System.out.println();
		// Printing all state wise actions
		System.out.println("Action Layer wise");
		for (Integer i : finalActions.keySet()) {
			System.out.println("Action "+ i + ":" + finalActions.get(i));
		}
		
		
		System.out.println();
		System.out.println("Layers Mutex wise");

		int i = 0;
		System.out.println();
		i = 0;
		// Printing Inconsistent Effects mutexes
		System.out.print("Inconsistent Effects mutexes at Level : \n\n");
		for (ArrayList ie : inconsistentEffectsMutexList) {
			System.out.print(i + ": ");
			System.out.println(ie);
			i++;
		}

		// Printing Interference mutexes
		System.out.println();
		i = 0;
		System.out.print("Interference mutexes at Level : \n\n");
		for (ArrayList im : interferenceMutexList) {
			System.out.print(i + ": ");
			System.out.println(im);
			i++;
		}
		
		// Printing Negated Literal
		System.out.println();
		System.out.print("Negated Literal mutexes at Level : \n\n");
		i = 0;
		for (ArrayList nl : negatedLiteralsMutexList) {
			System.out.print(i + ": ");
			System.out.println(nl);
			i++;
		}
	}

	private void parseInputFile(String filePath) throws FileNotFoundException {
		Scanner fileReader = new Scanner(new FileInputStream(filePath));
		String line = fileReader.nextLine();
		//Adding initial states
		String initLine = line.substring(5, line.length()).replace("+", "");
		String[] initSeparatedStates = initLine.split(",");
		fileReader.nextLine();
		String line2 = fileReader.nextLine();
		String goal = line2.substring(5, line2.length());
		fileReader.nextLine();	
		// Adding goal states
		for (String j : goal.split(",")) {
			if (j.contains("+")) {
				goalStates.add(j.substring(1, j.length()));
			} else {
				goalStates.add(j);
			}
		}
		states.addAll(Arrays.asList(initSeparatedStates));
		graphlayersStates.add(states);
		planerStates.addAll(Arrays.asList(initSeparatedStates));
		String lineNext = "";
		
		// Code to add Actions
		while (!(lineNext = fileReader.nextLine()).equals("")) {
			Graphlayer temp = new Graphlayer();
			temp.actionName = lineNext.substring(7, lineNext.length());
			String precond = fileReader.nextLine();
			for (String j : precond.substring(8, precond.length()).split(",")) {
				temp.preconditionList.add(j.contains("+") ? j.substring(1, j.length()) : j);
			}
			String effect = fileReader.nextLine();
			for (String j : effect.substring(7, effect.length()).split(",")) {
				temp.effectList.add(j.contains("+") ? j.substring(1, j.length()) : j);
			}
			for (String s : goalStates) {
				if (temp.effectList.contains(s)) {
					actionsPath.add(temp);
				}
			}
			graphActions.add(temp);
			if (!fileReader.hasNextLine()) {
				break;
			}
			fileReader.nextLine();
		}
	}

	// Method to check level off situation
	public boolean checkLevelOffCondition(ArrayList<String> goals, HashSet<String> set) {
		if (!allMutex.containsAll(goals) && set.containsAll(goals)) {
			return true;
		} else {
			return false;
		}	
	}

	// Method to identify actions of next layer
	public void findNextLayerActions(HashSet<Graphlayer> action, ArrayList<HashSet<String>> stateGraphlayers,
			ArrayList<HashSet<Graphlayer>> allActions, HashSet<Graphlayer> actionsPath) {

		HashSet<Graphlayer> currentActions = new HashSet<Graphlayer>();
		for (Graphlayer layer : action) {
			if (stateGraphlayers.get(stateGraphlayers.size() - 1).containsAll(layer.preconditionList)) {
				currentActions.add(layer);
			}
		}
		allActions.add(currentActions);
		finalStates.put(stateGraphlayers.size() - 1, stateGraphlayers.get(stateGraphlayers.size() - 1));
		persistOperation(currentActions, stateGraphlayers.get(stateGraphlayers.size() - 1));	
		findNextStatesOfPlan(allActions, stateGraphlayers);
		FindAndAddMutex(stateGraphlayers, allActions);
		findFinalActions(allActions);
	}

	// Method to identify final actions
	private void findFinalActions(ArrayList<HashSet<Graphlayer>> allActions) {
		HashSet<String> actionTemp = new HashSet<String>();
		for (Graphlayer currentAction : allActions.get(allActions.size() - 1)) {
			actionTemp.add(currentAction.actionName);
		}
		finalActions.put(allActions.size() - 1, actionTemp);
	}

	// Method to identify and add mutex
	// Currently have only added negated literal, inconsistent effect and interferenceCheck
	private void FindAndAddMutex(ArrayList<HashSet<String>> stateGraphlayers, ArrayList<HashSet<Graphlayer>> allActions) {
		negatedLiteralsCheck(stateGraphlayers.get(stateGraphlayers.size() - 1));
		inconsistentEffectsCheck(allActions.get(allActions.size() - 1));
		interferenceCheck(allActions.get(allActions.size() - 1));
		ArrayList<ArrayList> actionMutex = new ArrayList<>();
		actionMutex.addAll(inconsistentEffectsMutexList.get(inconsistentEffectsMutexList.size() - 1));
		actionMutex.addAll(interferenceMutexList.get(interferenceMutexList.size() - 1));

		allMutex.add(actionMutex);
	}

	// Method to handle persisted action
	public void persistOperation(HashSet<Graphlayer> currentActions, HashSet<String> currentStates) {
		for (String s : currentStates) {
			Graphlayer temp = new Graphlayer();
			temp.effectList.add(s);
			temp.actionName = "PersistAction(" + s +")";
			temp.preconditionList.add(s);
			currentActions.add(temp);
		}
	}
	
	// Method to find states of the next layer
	public void findNextStatesOfPlan(ArrayList<HashSet<Graphlayer>> allActions, ArrayList<HashSet<String>> stateGraphlayers) {
		HashSet<String> temp = new HashSet<>();
		for (Graphlayer n : allActions.get(allActions.size() - 1)) {
			temp.addAll(n.effectList);
		}
		stateGraphlayers.add(temp);
	}

	// Method to find negated literal check
		public void negatedLiteralsCheck(HashSet<String> layerStatesSet) {
		ArrayList<ArrayList> temp = new ArrayList<>();
		for (String mutex: layerStatesSet) {
			if(!mutex.contains("-") && layerStatesSet.contains("-"+mutex)) {
				temp.add(new ArrayList<String>(Arrays.asList(mutex, "-"+mutex)));
			}
		}
		negatedLiteralsMutexList.add(temp);
	}

		// Method to identify interference mutex
	public void interferenceCheck(HashSet<Graphlayer> allActions) {
		// TODO Auto-generated method stub
		ArrayList tempList1 = new ArrayList<>();
		ArrayList tempList2 = new ArrayList<>();
		for (Graphlayer n : allActions) {
			for (Graphlayer m : allActions) {
				if (!m.actionName.equals(n.actionName)) {
					for (String effect : n.effectList) {
						if (effect.contains("-")) {

							if (m.preconditionList.contains(effect.substring(1, effect.length()))) {
								tempList2.add(m.actionName);
								tempList2.add(n.actionName);
								tempList1.add(tempList2);
							}
						} else {
							if (m.preconditionList.contains("-" + effect)) {
								tempList2.add(m.actionName);
								tempList2.add(n.actionName);
								tempList1.add(tempList2);
							}
						}
					}

					tempList2 = new ArrayList<>();
				}
			}
		}
		interferenceMutexList.add(tempList1);
	}

	// Method to identify inconsistent effect mutex
	public void inconsistentEffectsCheck(HashSet<Graphlayer> AllActions) {
		
		ArrayList tempList1 = new ArrayList<>();
		ArrayList tempList2 = new ArrayList<>();
		HashSet<String> inconsistentEffectSet = new HashSet<String>();
		for (Graphlayer n : AllActions) {
			for (Graphlayer m : AllActions) {
				if (!m.actionName.equals(n.actionName)) {
					for (String effect : n.effectList) {

						if (effect.contains("-")) {
							if (m.effectList.contains(effect.substring(1, effect.length()))
									&& !inconsistentEffectSet.contains(n.actionName + m.actionName)) {
								tempList2.add(m.actionName);
								tempList2.add(n.actionName);
								inconsistentEffectSet.add(m.actionName + n.actionName);
								tempList1.add(tempList2);
							}
						} else {
							if (m.effectList.contains("-" + effect) && !inconsistentEffectSet.contains(n.actionName + m.actionName)) {
								tempList2.add(m.actionName);
								tempList2.add(n.actionName);
								inconsistentEffectSet.add(m.actionName + n.actionName);
								tempList1.add(tempList2);
							}
						}
					}

					tempList2 = new ArrayList<>();
				}
			}
		}
		inconsistentEffectsMutexList.add(tempList1);

	}

}
