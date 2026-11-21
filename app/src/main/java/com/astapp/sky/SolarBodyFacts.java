package com.astapp.sky;

/**
 * Solar system cards use NASA Science fact pages rather than synthetic generic
 * descriptions. The information is intentionally qualitative where exact numbers
 * change with time or discoveries.
 */
public final class SolarBodyFacts {
    private SolarBodyFacts(){}
    public static final int FIRST=22;
    public static final int PLANET_END=30;
    public static boolean isPlanet(int catalogIndex){return catalogIndex>=FIRST && catalogIndex<PLANET_END;}
    public static int planetIndex(int catalogIndex){return catalogIndex-FIRST;}
    public static boolean isMoon(int catalogIndex){return catalogIndex==30;}
    public static CosmicDetails.Info detail(int index){
        if(isPlanet(index)){
            int n=planetIndex(index);
            final String[] orbit={"0.39 AU","0.72 AU","1.00 AU","1.52 AU","5.20 AU","9.58 AU","19.2 AU","30.1 AU"};
            final String[] view={
                "태양 가까이 나타나므로 해가 진 직후나 뜨기 전 동·서쪽 낮은 하늘에서 관측해. 태양을 직접 바라보지 마.",
                "해 뜨기 전이나 해 진 후 밝게 빛나는 금성을 찾아봐. 대기 때문에 세부 표면은 가시광선으로 보기 어려워.",
                "지구는 우리가 관측하고 있는 행성이야. 저궤도 우주선과 위성 사진으로 대기와 구름을 관찰해.",
                "화성은 지구와 거리가 가까워지는 시기에 밝게 빛나. 붉은 별과 구별하려면 이동을 관측해봐.",
                "목성은 매우 밝으며 작은 망원경으로 갈릴레이 위성들과 구름 띠를 관찰할 수 있어.",
                "토성은 망원경으로 고리와 밝은 위성 타이탄을 관측할 수 있어. 고리 기울기는 시기에 따라 변해.",
                "천왕성은 맨눈으로 보기 매우 어렵고 쌍안경·망원경 및 정확한 위치 확인이 필요해.",
                "해왕성은 어두워서 망원경과 정확한 별지도가 필요해."
            };
            return new CosmicDetails.Info("태양까지 평균 거리 약 "+orbit[n],
                "태양계 · "+(n<4?"암석형 행성":n<6?"가스 거대행성":"얼음 거대행성"),
                PlanetFacts.SUMMARY[n]+"\n\n앱의 태양계 화면에서는 각 행성의 실제 계산 위치·거리·공전 운동을 별도로 확인할 수 있어. 관측사진은 탐사선이나 우주망원경이 촬영한 과거의 기록이며 현재 표면 영상은 아니야.",
                view[n],"NASA의 행성 관측 사진은 현재 시점의 표면이나 대기를 실시간으로 찍은 사진이 아니야.",PlanetFacts.URL[n]);
        }
        switch(index){
            case 30:return new CosmicDetails.Info("평균 약 38만 4,400km (지구 중심에서)",
                "지구의 자연위성","달은 지구 둘레를 공전하는 자연위성이야. 수많은 충돌구와 어두운 현무암 평원(바다)이 표면에 남아 있고, 지구에서 관측되는 위상은 달과 태양의 상대 위치 때문에 변해.",
                "쌍안경이나 망원경으로 명암의 경계인 터미네이터를 관찰하면 지형의 그림자가 선명하게 보여.",
                "달의 한쪽 면이 주로 보이는 것은 지구 공전 주기와 자전 주기가 동기화됐기 때문이야.",
                "https://science.nasa.gov/moon/");
            case 31:return new CosmicDetails.Info("태양에서 평균 약 39.5 AU",
                "카이퍼 벨트 · 왜행성","명왕성은 카이퍼 벨트에 있는 왜행성이야. 2015년 뉴허라이즌스 탐사선의 근접 관측은 질소 얼음으로 덮인 심장 모양 영역과 다양한 얼음 지형을 보여줬어. 공식 행성 8개에 포함되지는 않아.",
                "지구에서는 매우 어둡게 보여 큰 망원경과 촬영 장비가 필요해.",
                "행성에서 왜행성으로 재분류된 것은 명왕성이 사라졌다는 뜻이 아니라 분류 정의가 바뀐 거야.",
                "https://science.nasa.gov/dwarf-planets/pluto/");
            case 32:return new CosmicDetails.Info("목성에서 약 67만 km (궤도 반지름)",
                "목성의 위성","유로파는 바깥에 얼음 껍질이 있고 그 아래에 액체 바다가 있을 가능성이 높은 목성의 위성이야. 중력에 의한 조석 가열이 내부 에너지원으로 작용할 수 있어.",
                "작은 망원경으로는 목성 주변의 작은 빛점으로 보이며 표면 지형은 탐사선 사진에서 확인할 수 있어.",
                "얼음 아래 바다의 존재는 유로파가 생명체 탐사에서 중요한 목표가 된 이유 중 하나야.",
                "https://science.nasa.gov/jupiter/moons/europa/");
            case 33:return new CosmicDetails.Info("토성에서 약 122만 km (궤도 반지름)",
                "토성의 위성","타이탄은 두꺼운 질소 대기를 가진 토성의 거대한 위성이야. 표면에는 물이 아니라 액체 메탄·에탄이 만든 호수와 바다가 있으며 카시니-하위헌스 탐사로 자세히 연구했어.",
                "토성 옆 밝은 작은 점으로 망원경에서 확인할 수 있지만 지표면은 짙은 연무로 가려져.",
                "지구 바깥에서 표면에 안정적인 액체 호수가 확인된 대표적 천체야.",
                "https://science.nasa.gov/saturn/moons/titan/");
            case 34:return new CosmicDetails.Info("토성에서 약 24만 km (궤도 반지름)",
                "토성의 위성","엔셀라두스는 표면 대부분이 밝은 물 얼음으로 덮여 있는 작은 토성 위성이야. 남극 부근에서 수증기와 얼음 입자가 분출되는 현상이 관측됐고, 내부 바다의 가능성이 중요한 과학 연구 주제야.",
                "개인용 작은 망원경으로 세부 구조를 보기는 어려워. 카시니 탐사 사진이 가장 중요한 자료야.",
                "분출하는 얼음 입자는 토성 E고리의 물질 공급과도 연결돼 있어.",
                "https://science.nasa.gov/saturn/moons/enceladus/");
            default:return new CosmicDetails.Info("정보 없음","태양계","검증된 자료가 없습니다.","자료 확인 필요","정보 확인 필요","https://science.nasa.gov/solar-system/");
        }
    }
}