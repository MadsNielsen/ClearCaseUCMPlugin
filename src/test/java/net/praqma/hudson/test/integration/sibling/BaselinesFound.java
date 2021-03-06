package net.praqma.hudson.test.integration.sibling;


import net.praqma.hudson.test.BaseTestClass;
import org.junit.Rule;
import org.junit.Test;

import hudson.model.AbstractBuild;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.hudson.test.SystemValidator;

import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.hudson.CCUCMBuildAction;
import net.praqma.hudson.scm.pollingmode.PollSiblingMode;
import net.praqma.util.test.junit.TestDescription;


public class BaselinesFound extends BaseTestClass {
	
	@Rule
	public ClearCaseRule ccenv = new ClearCaseRule( "ccucm", "setup-interproject.xml" );
		
	public AbstractBuild<?, ?> initiateBuild( String projectName, boolean recommend, boolean tag, boolean description, boolean fail ) throws Exception {
        PollSiblingMode mode = new PollSiblingMode("INTIAL");
        mode.setCreateBaseline(true);
        mode.setUseHyperLinkForPolling(false);
		return jenkins.initiateBuild( projectName, mode, "_System@" + ccenv.getPVob(), "two_int@" + ccenv.getPVob(), recommend, tag, description, fail);
	}
    
    public AbstractBuild<?, ?> initiateBuildUsingHyperLink( String projectName, boolean recommend, boolean tag, boolean description, boolean fail ) throws Exception {
        PollSiblingMode mode = new PollSiblingMode("INTIAL");
        mode.setCreateBaseline(true);
        mode.setUseHyperLinkForPolling(true);
		return jenkins.initiateBuild( projectName, mode, "_System@" + ccenv.getPVob(), "two_int@" + ccenv.getPVob(), recommend, tag, description, fail);
	}

	@Test
	public void basicSibling() throws Exception {
		
		Stream one = ccenv.context.streams.get( "one_int" );
		Stream two = ccenv.context.streams.get( "two_int" );
		one.setDefaultTarget( two );
		
		/* The baseline that should be built */
		Baseline baseline = ccenv.context.baselines.get( "model-1" );
		
		AbstractBuild<?, ?> build = initiateBuild( "no-options-" + ccenv.getUniqueName(), false, false, false, false );

		/* Validate */
		SystemValidator validator = new SystemValidator( build ).
                validateBuild( build.getResult() ).
                validateBuiltBaseline( PromotionLevel.BUILT, baseline, false ).
                validateCreatedBaseline( true );
		validator.validate();
	}
    
    @Test
	public void basicSiblingUsingHlink() throws Exception {
	
		/* The baseline that should be built */
		Baseline baseline = ccenv.context.baselines.get( "model-1" );
		
		AbstractBuild<?, ?> build = initiateBuildUsingHyperLink( "no-options-hlink" + ccenv.getUniqueName(), false, false, false, false );
        
        build.getAction(CCUCMBuildAction.class).getBaseline();
		/* Validate */
		SystemValidator validator = new SystemValidator( build ).
                validateBuild( build.getResult() ).
                validateBuiltBaseline( PromotionLevel.BUILT, baseline, false ).                
                validateCreatedBaseline( true );
		validator.validate();
	}

    @Test
    @TestDescription(title = "Poll sibling with hyperlink", text = "poll sibling with hyperlink, build success, promote baseline and recommend")
	public void basicSiblingUsingHlinkRecommend() throws Exception {
	
		/* The baseline that should be built */
		Baseline baseline = ccenv.context.baselines.get( "model-1" );
        
		AbstractBuild<?, ?> build = initiateBuildUsingHyperLink( "hlink-recommend" + ccenv.getUniqueName(), true, false, false, false );

		/* Validate */
		SystemValidator validator = new SystemValidator( build ).
                validateBuild( build.getResult() ).
                validateBuiltBaseline( PromotionLevel.BUILT, baseline, false ).
                validateCreatedBaseline( true, true );
		validator.validate();
	}

    @Test
    @TestDescription(title = "Poll sibling", text = "poll sibling, build fails, reject baseline, no baseline created, no recommend")
	public void basicSiblingUsingHlinkDoNotRecommend() throws Exception {
        
        /* The baseline that should be built, and rejected because of failure */
		Baseline baseline = ccenv.context.baselines.get( "model-1" );

		AbstractBuild<?, ?> build = initiateBuildUsingHyperLink( "hlink-no-recommend" + ccenv.getUniqueName(), true, false, false, true );

		/* Validate */
		SystemValidator validator = new SystemValidator( build ).
                validateBuild( build.getResult() ).
                validateBuiltBaseline( PromotionLevel.REJECTED, baseline, false ).
                validateCreatedBaseline( false, false );
		validator.validate();
	}
}
