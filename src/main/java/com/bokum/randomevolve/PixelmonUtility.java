package com.bokum.randomevolve;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.EVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

public class PixelmonUtility
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random random = new Random();

    /** 무작위 포켓몬 생성 */
    public static Pokemon createRandomPokemon()
    {
        Pokemon pokemon = null;
        Species random_species = getRandomSpecies();

        try
        {
            // Pixelmon API의 팩토리 메서드를 사용하여 포켓몬 인스턴스 생성
            pokemon = PokemonFactory.create(random_species);
        }
        catch (Exception e)
        {
            LOGGER.error("createRandomPokemon error occurred. Species Info:\n", random_species.getJson().toString());
        }

        return pokemon;
    }

    /** 무작위 포켓몬 타입 얻기 */
    public static Species getRandomSpecies()
    {
        return PixelmonSpecies.getRandomSpecies(); //무작위 생물종 얻는 법
    }

    /** 무작위 포켓몬 특성 얻기 */
    public static Ability getRandomAbility()
    {
        try
        {
            // AbilityRegistry 클래스의 private static 필드인 REGISTERED_ABILITIES 맵을 가져온다
            Field mapField = AbilityRegistry.class.getDeclaredField("REGISTERED_ABILITIES");
            mapField.setAccessible(true); // private 접근 허용

            // 맵을 꺼내서 Map<String, AbilityData> 로 형변환한다
            @SuppressWarnings("unchecked")
            Map<String, ?> registryMap = (Map<String, ?>) mapField.get(null); // static 필드이므로 인스턴스 null

            // 모든 AbilityData 값을 리스트로 변환
            List<Object> allAbilityData = new ArrayList<>(registryMap.values());

            // 능력이 하나도 등록되어 있지 않으면 null 반환
            if (allAbilityData.isEmpty())
            {
                LOGGER.info("cannot get random ability. abilities is empty... registry size is " + registryMap.size());
                return null;
            }

            // 리스트에서 무작위로 하나의 AbilityData 객체 선택
            Object randomAbilityData = allAbilityData.get(random.nextInt(allAbilityData.size()));

            // AbilityData 클래스의 'instance' 필드를 리플렉션으로 가져옴
            Field instanceField = randomAbilityData.getClass().getDeclaredField("instance");
            instanceField.setAccessible(true); // private 접근 허용

            // instance 필드에서 실제 Ability 객체를 추출
            Ability ability = (Ability) instanceField.get(randomAbilityData);

            // 무작위로 얻은 Ability 반환
            return ability;
        }
        catch (Exception e)
        {
            // 리플렉션 중 오류가 발생한 경우 스택 트레이스 출력 후 null 반환
            e.printStackTrace();
            return null;
        }
    }

    /** 포켓몬 개체값 무작위로 설정 */
    public static void setRandomIVs(Pokemon pokemon)
    {
        IVStore ivStore = pokemon.getIVs();

        for (BattleStatsType stat : BattleStatsType.values())
        {
            int randomIV = random.nextInt(32); // 0에서 31 사이의 난수 생성
            ivStore.setStat(stat, randomIV);
        }
    }

    /** 포켓몬 노력치 무작위로 설정 */
    public static void setRandomEVs(Pokemon pokemon)
    {
        Random random = new Random();
        EVStore evStore = pokemon.getEVs();

        //기존 노력치 초기화
        for (BattleStatsType stat : BattleStatsType.values())
        {
            evStore.setStat(stat, 0);
        }

        List<BattleStatsType> stats = new ArrayList<>(Arrays.asList(BattleStatsType.values()));
        int remaining = 510;

        while (remaining > 0 && !stats.isEmpty())
        {
            BattleStatsType stat = stats.remove(random.nextInt(stats.size()));

            int maxForStat = Math.min(252, remaining); // 각 능력치당 최대 252
            int value = random.nextInt(maxForStat + 1); // 0 ~ maxForStat

            evStore.setStat(stat, value);
            remaining -= value;
        }
    }

    public static void changeFormRandomly(Pokemon pokemon, int percentage)
    {
        if(random.nextInt(100) > percentage)
        {
            return;
        }

        List<Stats> forms = pokemon.getSpecies().getForms();
        if(forms.size() <= 1)
        {
            return;
        }

        // 현재 폼 제외, 다른 폼 중에서 무작위 선택
        Stats current_form = pokemon.getForm();
        Stats random_form;
        do
        {
            random_form = forms.get(random.nextInt(forms.size()));
        } while (random_form.equals(current_form)); // 동일한 폼이면 다시 뽑기

        pokemon.setForm(random_form);
    }
}
