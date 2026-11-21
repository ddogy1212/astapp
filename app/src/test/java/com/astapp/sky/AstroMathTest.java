package com.astapp.sky;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.Instant;
import java.util.List;

public class AstroMathTest {
    static final long UTC=Instant.parse("2026-11-21T12:00:00Z").toEpochMilli();
    @Test public void jplCoordinatesStayFiniteWithinRange(){
        assertTrue(Ephemeris.inRange(UTC));
        for(int p=0;p<8;p++){
            double[] xyz=Ephemeris.position(p,UTC);
            assertEquals(3,xyz.length);
            for(double v:xyz)assertTrue(Double.isFinite(v));
            double magnitude=Ephemeris.norm(xyz);
            assertTrue(magnitude>0.25);
            assertTrue(magnitude<32);
        }
    }
    @Test public void innerOrbitsAreNotArtificiallyClusteredInGeometry(){
        assertTrue(Ephemeris.norm(Ephemeris.position(0,UTC))<.5);
        assertTrue(Ephemeris.norm(Ephemeris.position(1,UTC))>.6);
        assertTrue(Ephemeris.norm(Ephemeris.position(2,UTC))>.95);
        assertTrue(Ephemeris.norm(Ephemeris.position(3,UTC))>1.35);
    }
    @Test public void lunarFractionIsValidAndPeriodic(){
        double illuminated=MoonPhase.lit(UTC);
        assertTrue(illuminated>=0&&illuminated<=1);
        double later=MoonPhase.lit(UTC+(long)(29.530588853*86400000));
        assertEquals(illuminated,later,.00002);
    }
    @Test public void geocentricAltAzInRange(){
        for(int i=0;i<8;i++){
            if(i==2)continue;
            Observation.Coord c=Observation.planet(i,UTC,37.5,126.8);
            assertTrue(Double.isFinite(c.altitude));
            assertTrue(c.altitude>=-90&&c.altitude<=90);
            assertTrue(c.azimuth>=0&&c.azimuth<360);
        }
    }
    @Test public void plannerScoresAreRelativeAndSorted(){
        List<ObservingPlanner.Candidate> items=ObservingPlanner.rank(UTC,37.5,126.8,22);
        assertEquals(12,items.size());
        int previous=101;
        for(ObservingPlanner.Candidate c:items){
            assertTrue(c.score>=0&&c.score<=100);
            assertTrue(c.score<=previous);
            previous=c.score;
        }
    }
    @Test public void cloudsMakeObservationLessFavorable(){
        List<ObservingPlanner.Candidate> clear=ObservingPlanner.rank(UTC,37.5,126.8,0);
        List<ObservingPlanner.Candidate> cloudy=ObservingPlanner.rank(UTC,37.5,126.8,100);
        java.util.Map<String,Integer> scores=new java.util.HashMap<>();
        for(ObservingPlanner.Candidate item:clear)scores.put(item.name,item.score);
        for(ObservingPlanner.Candidate item:cloudy){
            assertTrue("clouds should not improve observing score for "+item.name,
                item.score<=scores.get(item.name));
        }
    }
    @Test public void impossibleTargetsGetZero(){
        for(ObservingPlanner.Candidate c:ObservingPlanner.rank(UTC,37.5,126.8,40)){
            if(c.coords.altitude<0)assertEquals(0,c.score);
        }
    }
}
