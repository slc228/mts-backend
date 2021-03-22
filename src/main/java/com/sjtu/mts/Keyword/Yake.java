package com.sjtu.mts.Keyword;

import com.sjtu.mts.Keyword.DataRepresentation.ComposedWord;
import com.sjtu.mts.Keyword.DataRepresentation.DataCore;
import com.sjtu.mts.Keyword.Wrapper.Levenshtein;
import com.sjtu.mts.Keyword.Wrapper.Tuple;
import com.sjtu.mts.Keyword.Wrapper.jellyfish;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sjtu.mts.Keyword.Wrapper.max;

public class Yake {

	public static class KeywordExtractorOutput {

		public static KeywordExtractorOutput from(String key, double value) {
			return new KeywordExtractorOutput(key, value);
		}

		public String key() {
			return this.key;
		}

		public double value() {
			return this.value;
		}

		@Override
		public String toString() {
			return String.format("(%s, %f)", this.key, this.value);
		}

		private String key;
		private double value;

		private KeywordExtractorOutput(String key, double value) {
			this.key = key;
			this.value = value;
		}
	}

	public enum DedupAlg {
		jaro,//"jaro_winkler","jaro"
		seqm,//"sequencematcher","seqm"
		levs,//"levenshtein", levs"
	}

	public static class KeywordExtractor {
		private String lan;
		private Set<String> stopword_set;
		private int n;
		private int top;
		private double dedupLim;
		private String[] features;
		private int windowsSize;
		private DedupFunc dedu_function;

		public KeywordExtractor(String lan, int n, double dedupLim, DedupAlg dedupFunc, int windowsSize, int top, String[] features) {
	        this.lan = lan;
	        String resource = String.format("StopwordsList/stopwords_%s.txt", this.lan.substring(0, 2).toLowerCase());
	        try {
	        	this.stopword_set = loadFromResource(resource, StandardCharsets.UTF_8);
	        } catch (Exception ex) {
	        	this.stopword_set = loadFromResource(resource, StandardCharsets.ISO_8859_1);
	        }
	        this.n = n;
	        this.top = top;
	        this.dedupLim = dedupLim;
	        this.features = features;
	        this.windowsSize = windowsSize;
	        if(dedupFunc == DedupAlg.jaro) {
	            this.dedu_function = this::jaro;
	        } else if(dedupFunc == DedupAlg.seqm) {
	            this.dedu_function = this::seqm;
	        } else {
	            this.dedu_function = this::levs;
	        }
		}

		public KeywordExtractor() {
			this("zh", 2, 1.0, DedupAlg.levs, 1, 25, null);
		}
		public KeywordExtractor(String lan, int n, double dedupLim, DedupAlg dedupFunc, int windowsSize, int top) {
			this(lan, n, dedupLim, dedupFunc, windowsSize, top, null);
		}

		public double jaro(CharSequence cand1, CharSequence cand2) {
			return jellyfish.jaro_winkler(cand1, cand2);
		}

		public double levs(CharSequence cand1, CharSequence cand2) {
			return 1.0 - jellyfish.levenshtein_distance(cand1, cand2) / max(cand1.length(), cand2.length());
		}

		public double seqm(CharSequence cand1, CharSequence cand2) {
			return Levenshtein.ratio(cand1, cand2);
		}

		public List<KeywordExtractorOutput> extract_keywords_output(String text) {
			text = text.replace("\n\t", " ");
			// 统计类的初始化，并做初步统计
			DataCore dc = new DataCore(text, this.stopword_set, this.windowsSize, this.n, null, null, this.lan);
			// 根据统计结果，更新single_word储存的数值，以及打分
			dc.build_single_terms_features(this.features);
			// 根据single_word得分，计算candidates的得分
			dc.build_mult_terms_features(this.features);
			List<Tuple<Object>> resultSet = new ArrayList<>();
			// candidates排序，得分越低越好
			List<ComposedWord> todedup = dc.candidates.values().stream()
					.filter(ComposedWord::isValid)
					.sorted((o1, o2) -> Double.compare(o1.H, o2.H))
					.collect(Collectors.toList());

			if (this.dedupLim >= 1.0) {
				return todedup.stream()
						.map(cand -> KeywordExtractorOutput.from(cand.unique_kw, cand.H))
						.limit(this.top).collect(Collectors.toList());
			}

			//以下为去重代码
			for (ComposedWord candidate : todedup) {
				boolean toadd = true;
				for (Tuple<Object> tuple : resultSet) {
					ComposedWord candResult = (ComposedWord) tuple.value(1);
					double dist = this.dedu_function.apply(candidate.unique_kw, candResult.unique_kw);
					if (dist > this.dedupLim) {
						toadd = false;
						break;
					}
				}
				if (toadd) {
					resultSet.add(Tuple.from(candidate.H, candidate));
				}
				if (resultSet.size() == this.top) {
					break;
				}
			}
			return resultSet.stream()
					.map(t -> KeywordExtractorOutput.from(((ComposedWord)t.value(1)).unique_kw, (double)t.value(0)))
					.collect(Collectors.toList());
		}

		public List<String> extract_keywords(String text) {
			List<KeywordExtractorOutput> tmplist = new ArrayList<>();
			tmplist = extract_keywords_output(text);
			List<String> returnList = new ArrayList<>();
			for (KeywordExtractorOutput output : tmplist){
				returnList.add(output.key());
			}
			return returnList;
		}

		private static interface DedupFunc {
			public double apply(CharSequence cand1, CharSequence cand2);
		}
	}

	public static Set<String> loadFromResource(String resource, Charset charset) {
		Set<String> result = new HashSet<>();
		try(InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) {
						continue;
					}
					result.add(line.toLowerCase());
				}
			} catch (Exception ex) {
				throw ex;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
}
