package ax.makila.comparableentititymining.postagger;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class StanfordPosTagger {
	// Initialize the tagger
	private static MaxentTagger tagger = new MaxentTagger(
			"edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");


	/**
	 * Splits the document into sentences, then splits each sentence into words
	 * after which POS tags are added.
	 * 
	 * @param question
	 *            The string to be tagged
	 * @return A list containing a list of sentences containing tokenized words
	 *         with associated POS tags
	 */
	public static List<ArrayList<TaggedWord>> tagString(String question) {
		List<List<HasWord>> sentences = null;
		sentences = MaxentTagger.tokenizeText(new StringReader(question));
		List<ArrayList<TaggedWord>> taggedWords = tagger.process(sentences);
		return taggedWords;
	}

	/**
	 * Tags a string but sets a special pos tag, "#" to #start and #end and merges any
	 * occurence of a lone $ and a c to $c. Adds #start and #end to each sentence if missing.
	 * @param question The input string
	 * @return A sentence separated array of tagged words
	 */
	public static List<List<CompTaggedWord>> tagStringHandleIdentifier(String question) {
		List<List<CompTaggedWord>> compTaggedWords = new ArrayList<List<CompTaggedWord>>();
		List<ArrayList<TaggedWord>> taggedWords = tagString(question);

		//Transform all taggedwords to my own tagged version which is better suited for this task
		for(int i = 0; i < taggedWords.size(); i++) {
			ArrayList<TaggedWord> innerList = taggedWords.get(i);
			List<CompTaggedWord> compInnerList = new ArrayList<CompTaggedWord>();
			for(TaggedWord word : innerList) {
				CompTaggedWord comp = new CompTaggedWord(word);
				compInnerList.add(comp);
			}
			compTaggedWords.add(compInnerList);
		}

		for(List<CompTaggedWord> wordList : compTaggedWords) {
			ListIterator<CompTaggedWord> it = wordList.listIterator();
			while(it.hasNext()) {
				CompTaggedWord word = it.next();
				if(word.value().equals("#start") || word.value().equals("#end")) {
					word.setTag("#");
				}
				//The tagger splits $c to $ and c so we have to merge them
				else if(word.value().equals("$") && it.hasNext()) {
					CompTaggedWord next = it.next();
					if(next.value().equals("c")) {
						it.remove();
						it.previous();
						word.setValue("$c");
						word.setTag(next.tag());
						word.setCompTag(CompTaggedWord.COMP_TAG);
					}
					else {
						it.previous();
					}
				}
			}
		}

		return compTaggedWords;

	}

	/**
	 * Tags a string but sets a special pos tag, "#" to #start and #end and merges any
	 * occurence of a lone $ and a c to $c. Adds #start and #end to each sentence if missing.
	 * @param question The input string
	 * @return A sentence separated array of tagged words
	 */
	public static List<List<CompTaggedWord>> tagStringHandleIdentifierAddLimiters(String question) {
		List<List<CompTaggedWord>> compTaggedWords = new ArrayList<List<CompTaggedWord>>();
		List<ArrayList<TaggedWord>> taggedWords = tagString(question);
		ListIterator<ArrayList<TaggedWord>> iterator = taggedWords.listIterator();

		while(iterator.hasNext()) {
			ArrayList<TaggedWord> word = iterator.next();
			//If the first token in each sentence doesn't match "#start", add "#start"
			if(!word.get(0).value().equals("#start")) {
				TaggedWord tagged = new TaggedWord("#start");
				tagged.setTag("#");
				tagged.setValue("#start");
				word.add(0, tagged);
			}
			//If the last token in each sentence doesn't match "#end", add "#end"
			if(!word.get(word.size() - 1).value().equals("#end")) {
				TaggedWord tagged = new TaggedWord("#end");
				tagged.setTag("#");
				tagged.setValue("#end");
				word.add(tagged);
			}
		}

		//Transform all taggedwords to my own tagged version which is better suited for this task
		for(int i = 0; i < taggedWords.size(); i++) {
			ArrayList<TaggedWord> innerList = taggedWords.get(i);
			List<CompTaggedWord> compInnerList = new ArrayList<CompTaggedWord>();
			for(TaggedWord word : innerList) {
				CompTaggedWord comp = new CompTaggedWord(word);
				compInnerList.add(comp);
			}
			compTaggedWords.add(compInnerList);
		}

		for(List<CompTaggedWord> wordList : compTaggedWords) {
			ListIterator<CompTaggedWord> it = wordList.listIterator();
			while(it.hasNext()) {
				CompTaggedWord word = it.next();
				if(word.value().equals("#start") || word.value().equals("#end")) {
					word.setTag("#");
				}
				//The tagger splits $c to $ and c so we have to merge them
				else if(word.value().equals("$") && it.hasNext()) {
					CompTaggedWord next = it.next();
					if(next.value().equals("c")) {
						it.remove();
						it.previous();
						word.setValue("$c");
						word.setTag(next.tag());
						word.setCompTag(CompTaggedWord.COMP_TAG);
					}
					else {
						it.previous();
					}
				}
			}
		}

		return compTaggedWords;

	}


	/**
	 * Tokenizes the input question into sentences and tokenize each sentence
	 * into words.
	 * 
	 * @param question
	 *            The string containing a question to be tokenized.
	 * @return A double list split into sentences containing words.
	 */
	public static List<List<String>> tokenizeString(String question) {
		List<List<String>> tags = new ArrayList<List<String>>();
		List<List<HasWord>> sentences = MaxentTagger
				.tokenizeText(new StringReader(question));
		List<ArrayList<TaggedWord>> taggedWords = tagger.process(sentences);
		for (int i = 0; i < taggedWords.size(); i++) {
			ArrayList<TaggedWord> sentence = taggedWords.get(i);
			ArrayList<String> inner = new ArrayList<String>();
			for (TaggedWord tag : sentence) {
				inner.add(tag.value());
			}
			tags.add(inner);
		}
		return tags;
	}

	/**
	 * Tokenizes the string and merge $ and c to $c. Adds #start and #end to each sentence.
	 * @param question The question to be tokenized
	 * @return The tokenized question
	 */
	public static List<List<String>> tokenizeStringMergeComp(String question) {
		List<List<String>> tokens = tokenizeString(question);
		List<String> token = new ArrayList<String>();
		for(List<String> t : tokens) {
			token.addAll(t);
		}
		ListIterator<String> it = token.listIterator();
		while(it.hasNext()) {
			String t = it.next();
			if(t.equals("$") && it.hasNext()) {
				String n = it.next();
				if(n.startsWith("c")) {
					it.remove();
					int prevIndex = it.previousIndex();
					it.previous();
					token.set(prevIndex, t + n);
				}
				else {
					it.previous();
				}
			}
			else if(t.matches("[\\.\\?\\!]") && it.hasNext()) {
				String n = it.next();
				if(n.equals("\\/") && it.hasNext()) {
					String m = it.next();
					if(m.equals(".")) {
						it.remove();
						it.previous();
						int prevIndex = it.previousIndex();
						it.remove();
						it.previous();
						token.set(prevIndex, t + n + m);
					}
					else {
						it.previous();
						it.previous();
					}
				}
				else {
					it.previous();
				}
			}
		}
		List<List<String>> tok = new ArrayList<List<String>>();
		tok.add(token);

		return tok;
	}

	/**
	 * Tokenizes the string and merge $ and c to $c. Adds #start and #end to each sentence.
	 * @param question The question to be tokenized
	 * @return The tokenized question
	 */
	public static List<List<String>> tokenizeStringMergeCompAddLimiters(String question) {
		List<List<String>> tokens = tokenizeString(question);
		for(List<String> token : tokens) {
			if(!token.get(0).equals("#start")) {
				token.add(0, "#start");
			}
			if(!token.get(token.size() - 1).equals("#end")) {
				token.add("#end");
			}
			ListIterator<String> it = token.listIterator();
			while(it.hasNext()) {
				String t = it.next();
				if(t.equals("$") && it.hasNext()) {
					String n = it.next();
					if(n.startsWith("c")) {
						it.remove();
						int prevIndex = it.previousIndex();
						it.previous();
						token.set(prevIndex, t + n);
					}
					else {
						it.previous();
					}
				}
			}
		}
		return tokens;
	}

	/**
	 * Returns a sequence representation of the tokenized input <tt>list</tt>.
	 * @param list A tokenized list of sentences and words
	 * @return A String representation of the tokenized list
	 */
	public static List<String> tokensToSequence(List<List<String>> list) {
		List<String> output = new ArrayList<String>();
		for(List<String> innerList : list) {
			StringBuilder sb = new StringBuilder();
			for(String token : innerList) {
				sb.append(token);
				sb.append(" ");
			}
			String string = PTBTokenizer.ptb2Text(sb.toString().trim());
			output.add(string);
		}

		return output;
	}

	/**
	 * Returns a string representation of the tokenized input <tt>list</tt>.
	 * @param list A tokenized list of sentences and words
	 * @return A String representation of the tokenized list
	 */
	public static String tokensToString(List<List<String>> list) {
		StringBuilder sb = new StringBuilder();
		for(List<String> innerList : list) {
			for(String token : innerList) {
				sb.append(token);
				sb.append(" ");
			}
		}

		String temp = sb.toString();
		temp = temp.replace("\\", "");

		String s  = PTBTokenizer.ptbToken2Text(sb.toString());

		if(temp.contains("?/.")) {
			s = s.replace("/.", "?/.");
		}
		else if(temp.contains("!/.")) {
			s = s.replace("/.", "!/.");
		}
		else if(temp.contains("./.")) {
			s = s.replace("/.", "./.");
		}



		return s.toString();
	}

	/**
	 * Returns a string representation of the tokenized input <tt>list</tt>. Removes sublists that doesn't
	 * match the regular expression <tt>regex</tt>.
	 * @param list A tokenized list of sentences and words
	 * @param regex A regular expression to matched against a string representation of a sublist
	 * @return A String representation of the tokenized list
	 */
	public static String tokensToString(List<List<String>> list, String regex) {
		Iterator<List<String>> it = list.iterator();
		while(it.hasNext()) {
			List<String> sub = it.next();
			if(!sub.toString().matches(regex)) {
				it.remove();
			}
		}

		return tokensToString(list);
	}




}
