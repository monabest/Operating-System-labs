
/**
 * Created by MonaBest on 1/29/17.
 */
import java.io.*;
import java.util.*;


public class Linker {

    static StringBuilder defined_never_used = new StringBuilder();
    static HashMap<String, Integer> use_exceed_module = new HashMap<>();

    static HashMap<String, Integer> defined_place = new HashMap<>();
    static HashSet<String> definition_exceeds_module = new HashSet<>();
    static HashMap<Integer, String> memory_defined_zero = new HashMap<>();
    static HashSet<Integer> multiple_variables = new HashSet<>();
    static HashSet<Integer> relative_exceeds_module = new HashSet<>();

    static HashSet<String> multi_defined = new HashSet<>();



    public static void main(String args[])throws Exception{


      //  File file = new File("./input/input-9.txt");
      //  Scanner input = new Scanner(file);
        Scanner input = new Scanner(System.in);
        int num_module = input.nextInt();

//1. first pass
//global definition map
        HashMap<String, Integer> map = new HashMap<>();
        ArrayList<Module> module_list= new ArrayList<>();

        for(int i = 0; i< num_module; i++) {
            Module m = new Module(); //order of module
            int num_def = input.nextInt();
            if (num_def != 0) {
                for (int j = 0; j < num_def; j++) {
                    String variable = input.next();
                    int relative_address = input.nextInt();
                    if (map.containsKey(variable)) {
                       multi_defined.add(variable);
                        continue;
                    }
                        m.defiList.add(variable);
                        map.put(variable, relative_address);
                        defined_place.put(variable, i);


                }
            }

            int num_use = input.nextInt();
            if(num_use != 0) {
                for (int z = 0; z < num_use; z++) {
                    String var = input.next();
                    int place = input.nextInt();
                    while (place != -1) {
                        m.useList.add(new Use(var, place));
                        place = input.nextInt();
                    }
                }
            }
            int num_Text = input.nextInt();
            if(num_Text != 0) {
                for (int a = 0; a < num_Text; a++) {
                    String type = input.next();
                    int word = input.nextInt();
                    m.programText.add(new Text(type, word));
                }
            }
            module_list.add(m);
        }


        //relative_address [0,1,2,3] -> module order[1,2,3,4] -> module list[0,1,2,3].
        int[] relative_address = new int[num_module];
        relative_address[0] = 0;
        for(int i = 1; i < num_module; i++){
            relative_address[i] = relative_address[i-1] + module_list.get(i-1).programText.size();
        }
        int num_modules = module_list.size();
        int module_size = relative_address[num_modules-1] + module_list.get(num_modules-1).programText.size();


        for(int i = 0; i< num_module; i++){
            if(module_list.get(i).defiList != null){
                for(String var : module_list.get(i).defiList){
                    int curModule_size = module_list.get(i).programText.size();
                    int address = map.get(var) + relative_address[i];   //update

                    if(map.get(var) >= curModule_size){
                        address = relative_address[i];      // if definition exceeds module size, first word is used.
                        definition_exceeds_module.add(var);
                    }
                    map.put(var, address);
                }
            }
        }



        //still need to update the variable's address.
        secondPass(module_list, map, relative_address);
        printResult(module_list, map, relative_address);

    }


    public static void secondPass(ArrayList<Module> modules, HashMap<String, Integer> map, int[] relativeAddress){


       HashSet<String> used_symbol = new HashSet<>();
        //first for each module Update type R
        for(int i = 0; i < modules.size(); i++) {
            Module now = modules.get(i);

            ArrayList<Text> TextList = now.programText;
            int text_length = TextList.size();

            if (TextList != null) {
                for (int j = 0; j < TextList.size(); j++) {
                    String instruction = TextList.get(j).type;
                    if (instruction.equals("R")) {
                        if(TextList.get(j).word % 1000 > text_length - 1){    //relative address is invalid
                            relative_exceeds_module.add(j + relativeAddress[i]);
                        }
                        TextList.get(j).word = TextList.get(j).word + relativeAddress[i];
                    }
                }
            }
            //use
            ArrayList<Use> use = now.useList;

            if(use != null){
                HashMap<Integer,String> text_place = new HashMap<>();

                for(int j = 0; j < use.size(); j++){
                    int loc = use.get(j).location;

                    if(loc > text_length -1){
                        use_exceed_module.put(use.get(j).symbol, i+1);
                        continue;
                    }
                    if(text_place.containsKey(loc)){
                        multiple_variables.add(use.get(j).location + relativeAddress[i]);
                        continue;
                    }
                    text_place.put(loc, use.get(j).symbol);

                    String var = use.get(j).symbol;
                    if(!map.containsKey(var)){
                        used_symbol.add(var);
                        int relative_loc = use.get(j).location;
                        TextList.get(relative_loc).word = TextList.get(relative_loc).word / 1000 * 1000 + 0;
                        memory_defined_zero.put((j+relativeAddress[i]), var);

                    }
                    else {
                        used_symbol.add(var);
                        int relative_loc = use.get(j).location;
                        TextList.get(relative_loc).word = TextList.get(relative_loc).word / 1000 * 1000 + map.get(var);
                    }

                 }
               }
            }

            //check if every symbol is used.
        for(String s : map.keySet()){
            if(!used_symbol.contains(s)){
                defined_never_used.append("Warning: " + s + " was defined in module " + (defined_place.get(s)+1) + " but never used.");
                defined_never_used.append("\n");
            }
        }

        }



    public static void printResult(ArrayList<Module> modules, HashMap<String, Integer> symbolTable, int[] relativeAddress){
        System.out.println("Symbol Table");
        for(Map.Entry<String, Integer> e : symbolTable.entrySet()){

            System.out.print(e.getKey() + "=" + e.getValue());
            if(multi_defined.contains(e.getKey())){
                System.out.print("  Error: This variable is multiply defined; first value used.");
            }
            if(definition_exceeds_module.contains(e.getKey())){
                System.out.print("  Error: Definition exceeds module size; first word in module used.");
            }

            System.out.println();
        }

        System.out.println();
        System.out.println("Memory Map");
        int index = 0;
        for(int i = 0; i< modules.size(); i++){

          ArrayList<Text> list = modules.get(i).programText;

            for(Text t : list){
                int curAddress = relativeAddress[i]+list.indexOf(t);

                if(t.type.equals("A")) {
                    if (t.word % 1000 > 200) {
                        System.out.print(index + ":" + t.word / 1000 * 1000 + " Error: Absolute address exceeds machine size; zero used.");
                    } else {
                        System.out.print(index + ":" + t.word);
                    }
                }
                else if(t.type.equals("R")){
                    if (relative_exceeds_module.contains(curAddress)) {
                        System.out.print(index + ":" + t.word / 1000 * 1000 + " Error: Relative address exceeds module size; zero used.");
                    } else {
                        System.out.print(index + ":" + t.word);
                    }
                }
                else{
                    System.out.print(index + ":" + t.word);
                }


                if(memory_defined_zero.containsKey(curAddress)){
                    System.out.print("  Error: " + memory_defined_zero.get(curAddress) +  " is not defined; zero used.");
                }
                if(multiple_variables.contains(curAddress)){
                    System.out.print(" Error: Multiple variables used in instruction; all but first ignored.");
                }
                System.out.println();
                index++;
            }
       }
       System.out.println();
       System.out.println(defined_never_used.toString());
        //use of variable exceeds module
        for(Map.Entry<String, Integer> e : use_exceed_module.entrySet()){
            String var = e.getKey();
            int module_palce = e.getValue();
            System.out.println("Error: Use of " + var + " in module " + module_palce + " exceeds module size; use ignored.");
        }


    }

}
