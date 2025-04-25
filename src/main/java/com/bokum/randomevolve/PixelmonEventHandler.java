package com.bokum.randomevolve;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.EvolveEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class PixelmonEventHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    public PixelmonEventHandler()
    {
        Pixelmon.EVENT_BUS.register(this); //Pixelmon 의 EVENT_BUS에 등록해야된다... 아오 이거땜에 2시간을 날림 ㅋㅋㅋㅋ
    }

    // Pixelmon 진화 이벤트 처리 (EvolveEvent 사용)
    @SubscribeEvent
    public void onEvolution(EvolveEvent.Post event)
    {
        Pokemon pokemon = event.getPokemon();
        if(pokemon == null)
        {
            return;
        }

        ServerPlayerEntity trainer_player = pokemon.getOwnerPlayer();
        if(trainer_player == null)
        {
            return; // 실제 플레이어의 포켓몬이 아닌 경우
        }

        //필요한 기존 정보 기록
        boolean is_shiny = pokemon.isShiny();

        //우선 무작위 포켓몬으로
        Species random_species = PixelmonUtility.getRandomSpecies();
        pokemon.setSpecies(random_species, false); //false 해야 스탯 등 초기화 안함

        //폼도 랜덤
        PixelmonUtility.changeFormRandomly(pokemon, 10); //확률은 10퍼

        //이로치면 이로치 그대로
        pokemon.setShiny(is_shiny);

        //무작위 특성 적용
        Ability random_ability = PixelmonUtility.getRandomAbility();
        pokemon.setAbility(random_ability);
        LOGGER.info("applying ability. " + random_ability.getName());

        //기술은 그대로

        //개체값 능력치 재설정
        PixelmonUtility.setRandomIVs(pokemon);

        //노력치 능력치 재설정
        //PixelmonUtility.setRandomEVs(pokemon); => 노력치는 그대로 해달라는 요구사항

        //레벨 동일

        //들고있는 템도 동일

        //파티 UI 상에서도 포켓몬 변경(새로고침)
        PokemonStorage storage = pokemon.getStorageAndPosition().getA();
        StoragePosition position = pokemon.getStorageAndPosition().getB();
        storage.set(position, pokemon);
    }

}
