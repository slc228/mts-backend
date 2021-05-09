package com.sjtu.mts.Keyword;


import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.sjtu.mts.Keyword.Wrapper.Tuple;
import com.sjtu.mts.Keyword.Wrapper.np;
import com.sjtu.mts.Keyword.Wrapper.nx;
import com.sjtu.mts.Keyword.Wrapper.nx_edge;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.sjtu.mts.Keyword.Wrapper.max;

public class DataRepresentation {
	public static final String STOPWORD_WEIGHT = "bi";
	//添加中文标点符号
	public static final char[] PUNCTUATION = "!\"#$%&\\'()*+,-./:;<=>?@[\\]^_`{|}~！。，“”‘’【】：、；？《》「」…".toCharArray();

	public static class Term {

		protected boolean stopword;
		protected int t;
		protected double tf;
		protected nx G;
		protected int id;
		protected double H;

		protected Term() {
			this.updateStopword();
		}

		public void updateH(String[] features, boolean isVirtual) {
		}

		public void updateH(double maxTF, double avgTF, double stdTF, int number_of_sentences, String[] features) {
		}

		public void addOccur(String tag, int sent_id, int pos_sent, int pos_text) {
		}

		protected Double getattr(String feature_name) {
			if ("tf".contentEquals(feature_name)) {
				return this.tf;
			}
			if ("t".contentEquals(feature_name)) {
				return (double)this.t;
			}
			if ("H".contentEquals(feature_name)) {
				return this.H;
			}
			if ("id".contentEquals(feature_name)) {
				return (double)this.id;
			}
			return null;
		}

		protected void updateStopword() {
			// TODO check stopword
		}
	}

	public static class SingleWord extends Term {
		protected String unique_term;
		protected double WFreq;
		protected double WCase;
		protected double tf_a;
		protected double tf_n;
		protected double WRel;
		protected double PL;
		protected double PR;
		protected double WPos;
		protected double WSpread;
		protected double pagerank;
		protected boolean stopword;
		protected Map<Integer, List<Tuple<?>>> occurs;

		public SingleWord(String unique, int idx, nx graph) {
			super();
			this.unique_term = unique;
			this.id = idx;
			this.tf = 0.0f;
			this.WFreq = 0.0f;
			this.WCase = 0.0f;
			this.tf_a = 0.0f;
			this.tf_n = 0.0f;
			this.WRel = 1.0f;
			this.PL = 0.f;
			this.PR = 0.f;
			this.occurs = new HashMap<>();
			this.WPos = 1.0f;
			this.WSpread = 0.0f;
			this.H = 0.0f;
			this.stopword = false;
			this.G = graph;
			this.pagerank = 1.0f;
		}

		public int WDR() {
			return this.G.out_edges(this.id).size();
		}

		public double WIR() {
			//return sum( [ d['TF'] for (u,v,d) in self.G.out_edges(self.id, data=True) ] );
			double sum = 0;
			for (nx_edge edge : this.G.out_edges(this.id)) {
				sum += (double)edge.d.get("TF");
			}
			return sum;
		}

		public double PWR() {
			double wir = this.WIR();
			if(wir == 0) {
				return 0;
			}
			return this.WDR() / wir;
		}

		public int WDL() {
			return this.G.in_edges(this.id).size();
		}

		public double WIL() {
			//return sum( [ d['TF'] for (u,v,d) in self.G.in_edges(self.id, data=True) ] )
			double sum = 0;
			for (nx_edge edge : this.G.in_edges(this.id)) {
				sum += (double)edge.d.get("TF");
			}
			return sum;
		}

		public double PWL() {
			double wil = this.WIL();
			if (wil == 0) {
				return 0;
			}
			return this.WDL() / wil;
		}

		@Override
		public void updateH(double maxTF, double avgTF, double stdTF, int number_of_sentences, String[] features) {
			boolean isNone 		= features == null || features.length == 0;
			boolean isWRel 		= false;
			boolean isWFreq 	= false;
			boolean isWSpread 	= false;
			boolean isWPos 		= false;
			boolean isWCase 	= false;
			if (features != null) {
				for (String feature : features) {
					if ("WRel".contentEquals(feature)) {
						isWRel = true;
					} else if ("WFreq".contentEquals(feature)) {
						isWFreq = true;
					} else if ("WSpread".contentEquals(feature)) {
						isWSpread = true;
					} else if ("isWCase".contentEquals(feature)) {
						isWCase = true;
					} else if ("WPos".contentEquals(feature)) {
						isWPos = true;
					}
				}
			}

			if(isNone || isWRel) {
				this.PL = this.WDL() / maxTF;
				this.PR = this.WDR() / maxTF;
				this.WRel = ((0.5 + (this.PWL() * (this.tf / maxTF))) + (0.5 + (this.PWR() * (this.tf / maxTF))));
			}
			if(isNone || isWFreq) {
				this.WFreq = this.tf / (avgTF + stdTF);
			}
			if(isNone || isWSpread) {
				this.WSpread = this.occurs.size() / number_of_sentences;
			}
			if (isNone || isWCase) {
				this.WCase = 0.1*(Math.max(this.tf_a, this.tf_n)) / (1. + Math.log(this.tf));
			}
			if(isNone || isWPos) {
				this.WPos = Math.log(Math.log(3. + np.median(this.occurs.keySet())));
			}

			this.H = (this.WPos * this.WRel) / (this.WCase + (this.WFreq / this.WRel) + (this.WSpread / this.WRel));
		}

		public void addOccur(String tag, int sent_id, int pos_sent, int pos_text) {
			if (!this.occurs.containsKey(sent_id)) {
				this.occurs.put(sent_id, new ArrayList<>());
			}
			this.occurs.get(sent_id).add(Tuple.from(pos_sent, pos_text));
			this.tf += 1.0;

			if("a".contentEquals(tag)) {
				this.tf_a += 1.0;
			}
			if("n".contentEquals(tag)) {
				this.tf_n += 1.0;
			}
		}


		@Override
		protected Double getattr(String feature_name) {
			Double value = super.getattr(feature_name);
			if (value != null) {
				return value;
			}

			if ("WFreq".contentEquals(feature_name)) {
				return this.WFreq;
			}
			if ("WCase".contentEquals(feature_name)) {
				return this.WCase;
			}
			if ("tf_a".contentEquals(feature_name)) {
				return this.tf_a;
			}
			if ("tf_n".contentEquals(feature_name)) {
				return this.tf_n;
			}
			if ("WRel".contentEquals(feature_name)) {
				return this.WRel;
			}
			if ("PL".contentEquals(feature_name)) {
				return this.PL;
			}
			if ("PR".contentEquals(feature_name)) {
				return this.PR;
			}
			if ("WPos".contentEquals(feature_name)) {
				return this.WPos;
			}
			if ("WSpread".contentEquals(feature_name)) {
				return this.WSpread;
			}
			if ("pagerank".contentEquals(feature_name)) {
				return this.pagerank;
			}
			// properties
			if ("WDR".contentEquals(feature_name)) {
				return (double)this.WDR();
			}
			if ("WIR".contentEquals(feature_name)) {
				return this.WIR();
			}
			if ("PWR".contentEquals(feature_name)) {
				return this.PWR();
			}
			if ("WDL".contentEquals(feature_name)) {
				return (double)this.WDL();
			}
			if ("WIL".contentEquals(feature_name)) {
				return this.WIL();
			}
			if ("PWL".contentEquals(feature_name)) {
				return this.PWL();
			}
			return null;
		}
	}

	public static class ComposedWord extends Term {
		protected boolean start_or_end_stopwords;
		protected Set<String> tags;
		protected String unique_kw;
		protected List<Term> terms;
		protected int size;
		protected double integrity;

		public ComposedWord(List<Tuple<Object>> terms) {// # [ (tag, word, term_obj) ]
			super();
			if (terms == null) {
				this.start_or_end_stopwords = true;
				this.tags = new HashSet<>();
				return;
			}
			List<String> tagBuilder = new ArrayList<>();
			List<String> unique_kwBuilder = new ArrayList<>();
			this.terms = new ArrayList<>();
			for (Tuple<Object> term : terms) {
				String tag = (String)term.value(0);
				String word = (String)term.value(1);
				Term term_obj = (Term)term.value(2);

				tagBuilder.add(tag);
				unique_kwBuilder.add(word.toLowerCase());
				if (term_obj != null) {
					this.terms.add(term_obj);//this.terms = [ w[2] for w in terms if w[2] != None ]
				}
			}
			this.tags = new HashSet<>();
			this.tags.add(String.join("",tagBuilder));//this.tags = set([''.join([ w[0] for w in terms ])])
			this.unique_kw = String.join(" ", unique_kwBuilder);//this.unique_kw = ' '.join( [ w[1].lower() for w in terms ] )
			this.size = terms.size();
			this.tf = 0.0;
			this.integrity = 1.0;
			this.H = 1.0;
			this.start_or_end_stopwords = this.terms.get(0).stopword || this.terms.get(this.terms.size() - 1).stopword;
		}

		public void uptadeCand(ComposedWord cand) {//(cand) :
			for (String tag : cand.tags) {// for tag in cand.tags: this.tags.add( tag )
				this.tags.add(tag);
			}
		}

		public boolean isValid() {
			boolean isValid = false;
			for (String tag : this.tags) {
				isValid = isValid || (!tag.contains("u") && !tag.contains("d"));//isValid or ( "u" not in tag and "d" not in tag )
			}
			return isValid && !this.start_or_end_stopwords;
		}

		public void updateH(String[] features, boolean isVirtual) {//(self, features=None, isVirtual=False):
			boolean isKPF = false;
			boolean isNone = features == null || features.length == 0 ? true : false;
			if (features != null) {
				for (String feature : features) {
					if ("KPF".contentEquals(feature)) {
						isKPF = true;
					}
				}
			}
			double sum_H  = 0.0;
			double prod_H = 1.0;
			for (int t = 0; t < this.terms.size(); t++) {
				Term term_base = this.terms.get(t);
				if(!term_base.stopword) {
					sum_H += term_base.H;
					prod_H *= term_base.H;
				} else {
					if("bi".contentEquals(STOPWORD_WEIGHT)) {
						double prob_t1 = 0.0;
						if((t - 1) >= 0 && term_base.G.has_edge(this.terms.get(t-1).id, this.terms.get(t).id)) {
							prob_t1 = term_base.G.prob(this.terms.get(t-1).id, this.terms.get(t).id, "TF") / this.terms.get(t-1).tf;
						}
						double prob_t2 = 0.0;
						if(term_base.G.has_edge(this.terms.get(t).id, this.terms.get(t+1).id)) {
							prob_t2 = term_base.G.prob(this.terms.get(t).id, this.terms.get(t+1).id, "TF") / this.terms.get(t+1).tf;
						}
						double prob = prob_t1 * prob_t2;
						prod_H *= (1 + (1 - prob ) );
						sum_H -= (1 - prob);
					} else if ("h".contentEquals(STOPWORD_WEIGHT)) {
						sum_H += term_base.H;
						prod_H *= term_base.H;
					} else if ("none".contentEquals(STOPWORD_WEIGHT)) {
					}
				}
			}
			double tf_used = 1.0;
			if(isNone || isKPF) {
				tf_used = this.tf;
			}
			if(isVirtual) {
				tf_used = np.mean(terms.stream().map(term_obj -> term_obj.tf).collect(Collectors.toList()));//np.mean( [term_obj.tf for term_obj in self.terms] )
			}
			this.H = prod_H / ( ( sum_H + 1 ) * tf_used );
		}

		public void updateH(String[] features) {
			this.updateH(features, false);
		}

		public void updateH() {
			this.updateH(null);
		}



		@Override
		protected Double getattr(String feature_name) {
			Double value = super.getattr(feature_name);
			if (value != null) {
				return value;
			}
			if ("integrity".contentEquals(feature_name)) {
				return this.integrity;
			}
			if ("size".contentEquals(feature_name)) {
				return (double)this.size;
			}
			return null;
		}
	}

	public static class DataCore {
		protected int number_of_sentences;
		protected int number_of_words;
		protected Map<String, Term> terms;
		protected Map<String, ComposedWord> candidates;
		protected List<Object> sentences_obj;
		protected List<List<com.hankcs.hanlp.seg.common.Term>> sentences_str;
		protected nx G;
		protected char[] exclude;
		protected String[] tagsToDiscard;
		protected Map<Integer, Double> freq_ns;
		protected Set<String> stopword_set;
		protected String language;

		public DataCore(String text, Set<String> stopword_set, int windowsSize, int n, String[] tagsToDiscard, char[] exclude, String language) {
			this.number_of_sentences = 0;
			this.number_of_words = 0;
			this.terms = new HashMap<>();
			this.candidates = new HashMap<>();
			this.sentences_obj = new ArrayList<>();
			this.sentences_str = new ArrayList<>();
			this.G = nx.DiGraph();
			this.exclude = exclude == null ? PUNCTUATION : exclude;
			this.tagsToDiscard = tagsToDiscard == null ? new String[] {"u", "d"} : tagsToDiscard;
			this.freq_ns = new HashMap<>();
			for (int i = 0; i < n; i++) {
				this.freq_ns.put(i+1, 0.0);
			}
			this.stopword_set = stopword_set;
			this.language = language;
			this.build_features(text, windowsSize, n);
		}

		public void build_features(String text, int windowsSize, int n) {
			text = this.pre_filter(text);
			//分句分词，得到sentences_str(类型为List<List<String>>)
			this.sentences_str = get_sentences_str(text);
			this.number_of_sentences = this.sentences_str.size();
			// 用于计算文章共有多少个单词（不去重）
			int pos_text = 0;
			// 可以看作当前遍历的缓冲区，如果遇到标点符号，则缓冲区清空
			List<Tuple<Object>> block_of_word_obj = new ArrayList<>();
			// 每个句子的缓冲区，将block_of_word_obj中的内容存入句子中
			List<Object> sentence_obj_aux = new ArrayList<>();

			// 遍历句子
			for (int sentence_id = 0; sentence_id < this.sentences_str.size(); sentence_id++) {
				List<com.hankcs.hanlp.seg.common.Term> sentence = this.sentences_str.get(sentence_id);
				sentence_obj_aux = new ArrayList<>();
				block_of_word_obj = new ArrayList<>();
				// 遍历词
				for (int pos_in_sentence = 0; pos_in_sentence < sentence.size(); pos_in_sentence++) {
					String word = sentence.get(pos_in_sentence).word;
					// 判断当前词是否全部由符号构成
					int cExclude = 0;
					for (char c : word.toCharArray()) {
						for (char e : this.exclude) {
							if (c == e) {
								cExclude++;
							}
						}
					}
					if (cExclude == word.length()) {
						if (block_of_word_obj.size() > 0) {
							sentence_obj_aux.add( block_of_word_obj );
							block_of_word_obj = new ArrayList<>();
						}
					} else {
						String tag = this.getTag(sentence.get(pos_in_sentence), pos_in_sentence);
						Term term_obj = this.getTerm(word);
						term_obj.addOccur(tag, sentence_id, pos_in_sentence, pos_text);
						pos_text += 1;
						// Create co-occurrence matrix
						// tag not in this.tagsToDiscard:
						if (Arrays.stream(this.tagsToDiscard).filter(item -> item.equals(tag)).findFirst().orElse(null) == null) {
							int[] word_windows = IntStream.range(max(0, block_of_word_obj.size()- windowsSize), block_of_word_obj.size()).toArray();
							for (int w : word_windows) {
								Tuple<Object> wt = block_of_word_obj.get(w);
								String wTag = (String)wt.value(0);
								//block_of_word_obj[w][0] not in self.tagsToDiscard
								if (Arrays.stream(this.tagsToDiscard).filter(item -> item.equals(wTag)).findFirst().orElse(null) == null) {
									this.addCooccur((Term)wt.value(2), term_obj);
								}
							}
						}
						// Generate candidate keyphrase list
						List<Tuple<Object>> candidate = new ArrayList<>();
						candidate.add(Tuple.from(tag, word, term_obj));
						ComposedWord cand = new ComposedWord(candidate);
						this.addOrUpdateComposedWord(cand);

						// 以当前词为基准，倒序枚举windowSize下出现过的词。相当于将[n],[n-1,n],[n-2,n-1,n]......依次更新。
						for (int w =  block_of_word_obj.size() - 1; w >= max(0, block_of_word_obj.size() - (n - 1)); w--) {
							candidate.add(block_of_word_obj.get(w));
							int key = candidate.size();
							this.freq_ns.put(key, this.freq_ns.get(key) + 1.0);
							List<Tuple<Object>> invertCandidate = new ArrayList<>();
							for (int i = candidate.size() - 1; i >= 0; i--) {
								invertCandidate.add(candidate.get(i));
							}
							cand = new ComposedWord(invertCandidate);
							this.addOrUpdateComposedWord(cand);
						}
						block_of_word_obj.add(Tuple.from(tag, word, term_obj));
					}
				}
				if (block_of_word_obj.size() > 0) {
					sentence_obj_aux.add(block_of_word_obj);
				}
				if (sentence_obj_aux.size() > 0) {
					this.sentences_obj.add(sentence_obj_aux);
				}
			}
			if (block_of_word_obj.size() > 0) {
				sentence_obj_aux.add( block_of_word_obj );
			}

			if (sentence_obj_aux.size() > 0) {
				this.sentences_obj.add(sentence_obj_aux);
			}
			this.number_of_words = pos_text;
		}

		public void build_single_terms_features(String[] features) {
			List<Double> termTFs = new ArrayList<>();
			List<Double> termValidTFs = new ArrayList<>();
			this.terms.values().stream()
					.peek(term -> termTFs.add(term.tf))
					.filter(term -> !term.stopword)
					.peek(term -> termValidTFs.add(term.tf))
					.collect(Collectors.toList());// [ term for term in self.terms.values() if not term.stopword ]
			np validTFs = np.array(termValidTFs);//(np.array([ x.tf for x in validTerms ]))
			double avgTF = validTFs.mean();
			double stdTF = validTFs.std();
			double maxTF = max(termTFs);//max([ x.tf for x in self.terms.values()])
			this.terms.values().stream()
					.forEach(term -> term.updateH(maxTF, avgTF, stdTF, this.number_of_sentences, features));
		}

		public void build_single_terms_features() {
			this.build_single_terms_features(null);
		}

		public void build_mult_terms_features(String[] features) {
			this.candidates.values().stream()
					.filter(ComposedWord::isValid)
					.forEach(cand -> cand.updateH(features));
		}

		public void build_mult_terms_features() {
			this.build_mult_terms_features(null);
		}

		public String pre_filter(String text) {
			Pattern prog = Pattern.compile("^(\\s*([A-Z]))");
			String[] parts = text.split("\n");
			StringBuffer buffer = new StringBuffer();
			for(String part : parts) {
				String sep = " ";
				if(prog.matcher(part).find()) {
					sep = "\n\n";
				}
				buffer.append(sep).append(part.replace('\t',' '));
			}
			return buffer.toString();
		}

		public List<List<com.hankcs.hanlp.seg.common.Term>> get_sentences_str(String text){
			List<List<com.hankcs.hanlp.seg.common.Term>> sentences_str = new ArrayList<>();
			String[] sentences = text.split("[。！？]");
			for (String sentence : sentences) {
				List<com.hankcs.hanlp.seg.common.Term> words = HanLP.segment(sentence);
				sentences_str.add(words);
			}
			return sentences_str;
		}

		public String getTag(com.hankcs.hanlp.seg.common.Term term, int i) {
			String word = term.word;
			Nature word_nature = term.nature;
			if (word_nature.equals("o"))
			{
				System.out.println(word);
				System.out.println(word_nature);
			}
			if (word_nature.startsWith("nr") || word_nature.startsWith("g")
					|| word_nature.startsWith("nb") || word_nature.startsWith("nh")
					|| word_nature.startsWith("ni") || word_nature.startsWith("nm")
					|| word_nature.startsWith("nr") || word_nature.startsWith("ns")
					|| word_nature.startsWith("nt") || word_nature.startsWith("nx")
					|| word_nature.startsWith("nz")){
				return "a";
			}
			try {
				String w2 = word.replace(",","");
				Float.parseFloat(w2);// float(w2)
				return "d";
			} catch (Exception ex) {
				int cdigit = 0;
				int calpha = 0;
				int cexclude = 0;
				int csuper = 0;
				char[] words = word.toCharArray();
				for (char c : words) {
					if (Character.isDigit(c)) {	//isdigit
						cdigit++;
					}
					if (Character.isAlphabetic(c)) {	//isalpha
						calpha++;
					}
					if (Character.isUpperCase(c)) {	//issuper
						csuper++;
					}
					for (char _c : this.exclude) {	//c in this.exclude
						if (c == _c) {
							cexclude++;
						}
					}
				}
				if ((cdigit > 0 && calpha > 0) || (cdigit == 0 && calpha == 0) || cexclude > 1) {
					return "u";
				}
				if(word.length() == csuper) {
					return "a";
				}
				if (csuper == 1 && word.length() > 1 && Character.isUpperCase(words[0]) && i > 0) {
					return "n";
				}
			}
			return "p";
		}

		public Term getTerm(String str_word) {
			//判断是否为停用词（中文中，认为长度 <= 1的词为停用词）
			String unique_term = str_word.toLowerCase();
			boolean simples_sto = this.stopword_set.contains(unique_term);
			if(unique_term.endsWith("s") && unique_term.length() > 3) {
				unique_term = unique_term.substring(0, unique_term.length() - 1);
			}
			if(this.terms.containsKey(unique_term)) {
				return this.terms.get(unique_term);
			}
			String simples_unique_term = unique_term;
			for(char pontuation : this.exclude) {
				simples_unique_term = simples_unique_term.replace(pontuation + "", "");
			}
			boolean isstopword = simples_sto || this.stopword_set.contains(unique_term) || simples_unique_term.length() <= 1;

			// this.terms 类型为 Map<String, Term>
			int term_id = this.terms.size();
			Term term_obj = new SingleWord(unique_term, term_id, this.G);
			term_obj.stopword = isstopword;

			this.G.add_node(term_id);
			this.terms.put(unique_term, term_obj);
			return term_obj;
		}

		public void addCooccur(Term left_term, Term right_term) {
			if(!this.G.check_contain(left_term.id, right_term.id)) {
				Map<String, Object> d = new HashMap<>();
				d.put("TF", 0.0);
				this.G.add_edge(left_term.id, right_term.id, d);
			}
			double prob = this.G.prob(left_term.id, right_term.id, "TF");
			this.G.prob(left_term.id, right_term.id, "TF", prob + 1.0);
		}

		public void addOrUpdateComposedWord(ComposedWord cand) {
			String key = cand.unique_kw;
			if(!this.candidates.containsKey(key)) {
				this.candidates.put(key, cand);
			} else {
				this.candidates.get(key).uptadeCand(cand);
			}
			this.candidates.get(key).tf += 1.0;
		}
	}
}
