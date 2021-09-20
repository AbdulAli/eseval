

public class ConfusionMatrix {
	
	int TP_buggy;  //int nblb
	int TN_buggy; //int nclc
	int FP_buggy; //int nclb;
	int FN_buggy; //int nblc;
	int TP_clean; 
	int TN_clean; 
	int FP_clean; 
	int FN_clean; 
	
	public ConfusionMatrix(int nc, int nb, int nblb, int nclc, int nclb, int nblc ) {
		
		TN_clean = TP_buggy = nblb;
		TP_clean = TN_buggy = nclc;   
		FN_clean = FP_buggy = nclb;
		FP_clean = FN_buggy = nblc;
		
		//System.out.println(nclean+"--"+nbuggy+" TP_clean:"+TP_clean+" 			TN_clean:"+TN_clean+" FP_clean:"+FP_clean+" FN_clean"+FN_clean);
	}
		
	public float calPrecBuggy() {
		return ((float)TP_buggy/(TP_buggy+FP_buggy));
	}
			
	public float calRecallBuggy() {
		return ((float)TP_buggy/(TP_buggy+FN_buggy));
	}
	
	public float FScoreBuggy() {
		System.out.println("Prec Buggy:"+calPrecBuggy());
		System.out.println("Recall Buggy:"+calRecallBuggy());
		return 2*(calPrecBuggy()*calRecallBuggy())/(calPrecBuggy()+calRecallBuggy());
	}
	
	public float calPrecClean() {
		return (float)TP_clean/(TP_clean+FP_clean);
	}
	
	public float calRecallClean() {
		return (float)TP_clean/(TP_clean+FN_clean);
	}
	
	public float FScoreClean() {
		System.out.println("Prec Clean:"+calPrecClean());
		System.out.println("Recall Clean:"+calRecallClean());
		return 2*calPrecClean()*calRecallClean()/(calPrecClean()+calRecallClean());
	}
	
	public float calPrec() {
		return (float) (TP_buggy)/((TP_buggy)+(FP_buggy));

	}
	
	public float calRecall() { 
		return ((float)(TP_buggy)/((TP_buggy)+(FN_buggy)));
	}

	
	public float FScore() {
		System.out.println("Prec Overall:"+calPrec());
		System.out.println("Recall Overall:"+calRecall());
		return 2*calPrec()*calRecall()/(calPrec()+calRecall());
	}
	
	public float calAccuracy() {
		return (float)(TP_clean+TN_clean)/(TP_clean+TN_clean+FP_clean+FN_clean);
	}
	
}
