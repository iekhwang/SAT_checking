import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.*;


class Solver {

    public ArrayList<ArrayList<Integer>> parse_dimacs(String filename) throws IOException {

        ArrayList<ArrayList<Integer>> clauses = new ArrayList<ArrayList<Integer>>();
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("c")) continue;
            if (line.startsWith("p")) continue;
            ArrayList<Integer> clause = new ArrayList<Integer>();
            for (String literal: line.split("\\s+")) {
                Integer lit = new Integer(literal);
                if (lit == 0) break;
                clause.add(lit);
            }
            if (clause.size() > 0) {
                clauses.add(clause);
            }
        }
        return clauses;
    }

    public static Map<Integer,Integer> counter(ArrayList<ArrayList<Integer>> list){
    	
        ArrayList<Integer> literals = new ArrayList<Integer>();
        Map<Integer,Integer> counts = new HashMap();
        for (int i = 0; i <list.size();i++){
            ArrayList<Integer> lit = list.get(i);
            for (int j=0; j < lit.size();j++){
                if(!literals.contains(list.get(i).get(j))){
                    literals.add(list.get(i).get(j));
                    counts.put(list.get(i).get(j),1);
                }
                else{
                    counts.put(list.get(i).get(j),counts.get(list.get(i).get(j))+1);
                }
            }
        }
        return counts;
    }

    public static Map<String,ArrayList<ArrayList<Integer>>> pure(ArrayList<ArrayList<Integer>> list) {
    	
        Map<Integer,Integer> counts = counter(list);
        Map<String,ArrayList<ArrayList<Integer>>> map =new HashMap();
        ArrayList<Integer> literals = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> assignment = new ArrayList<ArrayList<Integer>>(1);
        ArrayList<Integer> pures = new ArrayList<Integer>();
        for (Map.Entry<Integer, Integer> entry: counts.entrySet())
            literals.add(entry.getKey());
        for (Map.Entry<Integer, Integer> entry: counts.entrySet()) {
            if(!literals.contains(- entry.getKey())){
                pures.add(entry.getKey());
            }
        }
        for (int i = 0; i< pures.size();i++) {
            list = bcp(list, pures.get(i));
        }
        assignment.add(pures);
        map.put("0",list);
        map.put("1",assignment);
        return map;
    }

    public static ArrayList<ArrayList<Integer>> bcp(ArrayList<ArrayList<Integer>> list, int unit) {
    	
        ArrayList<ArrayList<Integer>> new_list = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i< list.size(); i++){
            if(list.get(i).contains(unit))
                continue;
            if(list.get(i).contains(-unit)){
                ArrayList<Integer> c = new ArrayList<Integer>();
                for(int j=0; j<list.get(i).size();j++ ){
                    if(list.get(i).get(j)==-unit)
                        continue;
                    else
                        c.add(list.get(i).get(j));
                }
                if(c.size()==0)
                    return null;
                new_list.add(c);
            }
            else{
                new_list.add(list.get(i));
            }
        }
        return new_list;
    }

    public static Map<String,ArrayList<ArrayList<Integer>>> unit_propagation(ArrayList<ArrayList<Integer>> list){
    	
        ArrayList<ArrayList<Integer>> assignment = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> unit = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> unit_clauses = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> empty_list = new ArrayList<ArrayList<Integer>>();
        Map<String,ArrayList<ArrayList<Integer>>> map =new HashMap();
        ArrayList<Integer> empty_list1 = new ArrayList<Integer>();
        assignment.add(empty_list1);
        for (int i=0; i< list.size();i++){
            if (list.get(i).size()==1){
                unit_clauses.add(list.get(i));
            }
        }
        while(unit_clauses.size()>0){
            unit = unit_clauses.get(0);
            list = bcp(list, unit.get(0));
            assignment.get(0).add(unit.get(0));
            if(list == null){
                map.put("0",null);
                map.put("1",empty_list);
                return map;
            }
            if(list.size()==0){
                map.put("0",list);
                map.put("1",assignment);
                return map;
            }
            unit_clauses.clear();
            for (int i=0; i< list.size();i++){
                if (list.get(i).size()==1){
                    unit_clauses.add(list.get(i));
                }
            }
        }
        map.put("0",list);
        map.put("1",assignment);

        return map;
    }

    public static int var_sec(ArrayList<ArrayList<Integer>> list){
    	
        Map<Integer,Integer> counts = new HashMap();
        counts = counter(list);
        List<Map.Entry<Integer,Integer>> list_sort = new ArrayList(counts.entrySet());
        Collections.sort(list_sort, (o1, o2) -> (o1.getValue() - o2.getValue()));
        return list_sort.get(list_sort.size()-1).getKey();

    }

    public static ArrayList<Integer> backtracking(ArrayList<ArrayList<Integer>> list, ArrayList<ArrayList<Integer>> assignment) {
    	
        ArrayList<ArrayList<Integer>> new_list = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> pure_assignment = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> unit_assignment = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> assignment_1d = new ArrayList<Integer>();
        ArrayList<Integer> empty_list = new ArrayList<Integer>();
        ArrayList<Integer> result = new ArrayList<Integer>();
        Map<String,ArrayList<ArrayList<Integer>>> map =new HashMap();
        ArrayList<Integer> var = new ArrayList<Integer>();
        map = pure(list);
        pure_assignment = map.get("1");
        map = unit_propagation(map.get("0"));
        unit_assignment = map.get("1");
        new_list = map.get("0");
        if(pure_assignment.size()==1)
            assignment_1d.addAll(pure_assignment.get(0));
        if(unit_assignment.size()==1)
            assignment_1d.addAll(unit_assignment.get(0));
        if(assignment.size()==1)
            assignment.get(0).addAll(assignment_1d);
        if (new_list == null)
            return empty_list;
        if (new_list.size() == 0) 
            return assignment.get(0);
        int variable = var_sec(new_list);
        if (assignment.size()== 0){
            var.add(variable);
            assignment.add(var);
        }else{
            assignment.get(0).add(variable);
        }
        result = backtracking(bcp(new_list,variable), assignment);

        if (result.size() == 0) {
            assignment.get(0).add(-variable);
            result = backtracking(bcp(new_list, -variable), assignment);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
    	
        Solver solver = new Solver();
        ArrayList<ArrayList<Integer>> clauses = solver.parse_dimacs(args[0]);
        ArrayList<ArrayList<Integer>> cls = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> literals = new ArrayList<Integer>();
        ArrayList<Integer> result = Solver.backtracking(clauses,cls);
        Map<Integer,Integer> counts = Solver.counter(clauses);
        for (Map.Entry<Integer, Integer> entry: counts.entrySet()) {
            literals.add(entry.getKey());
        }
        if (result.size()!=0){
            System.out.print("sat ");
            for (int i=1; i <=literals.size()/2; i++){
                if(result.contains(-i))
                    System.out.print(-i + " ");
                else
                    System.out.print(i + " ");
            }
            System.out.println();
            System.exit(10);
        }else{
            System.out.println("unsat");
            System.exit(20);
        }
    }
}
