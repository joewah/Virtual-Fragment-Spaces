import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.actelion.research.chem.Molecule;
import com.actelion.research.chem.StereoMolecule;
import com.actelion.research.chem.chemicalspaces.ChemicalSpaceCreator;
import com.actelion.research.chem.io.RXNFileParser;
import com.actelion.research.chem.io.SDFileParser;
import com.actelion.research.chem.reaction.Reaction;

public class VSCreator {

	public static void main(String[] args) throws FileNotFoundException {
		String workDir = "C:\\Users\\wahljo1\\Manuscript_CombiChemSpaces";
		RXNFileParser rxnParser = new RXNFileParser();
		File dir = new File(workDir);
		List<Reaction> reactions = new ArrayList<>();
		/*
		 * parsing the files with the reaction definitions (.rxn files)
		 */
		for(File reactionFile : dir.listFiles()) {
			if(!reactionFile.getName().endsWith(".rxn"))
				continue;
			Reaction reaction = new Reaction();
			String reactionName = reactionFile.getName().split("\\.")[0];
			reaction.setName(reactionName);
			BufferedReader reader = new BufferedReader(new FileReader(reactionFile));
			try {
				rxnParser.parse(reaction, reader);
			}
			catch(Exception e) {
				continue;
			}
			reactions.add(reaction);
		}
		/*
		 * parsing the file with the building blocks
		 */
		String bbFile = "Enamine_Building_Blocks.sdf";
		Set<String> bbs = new HashSet<>();
		SDFileParser parser = new SDFileParser(workDir + "\\" + bbFile);
		String[] columns = parser.getFieldNames();
		parser.close();
		parser = new SDFileParser(workDir + "\\" + bbFile,columns);
		int idField = parser.getFieldIndex("Enamine-ID");
		Map<String,Map<String,List<String>>> bbData = new HashMap<String,Map<String,List<String>>>();
		while(parser.next()) {
			String enamineID = parser.getFieldData(idField);
			StereoMolecule bb = parser.getMolecule();
			bb.ensureHelperArrays(Molecule.cHelperParities);
			bbs.add(bb.getIDCode());
			bbData.putIfAbsent(bb.getIDCode(), new HashMap<String,List<String>>());
			Map<String,List<String>> propertyMap = bbData.get(bb.getIDCode());
			propertyMap.putIfAbsent("Enamine-ID", new ArrayList<>());
			propertyMap.get("Enamine-ID").add(enamineID);
			
		}
		/*
		 * create the space
		 */
		ChemicalSpaceCreator creator = new ChemicalSpaceCreator(bbs,reactions,dir);
		creator.setBBData(bbData);
		creator.create();
		
	}
}
