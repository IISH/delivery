package org.socialhistoryservices.delivery.util;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.SingleColorGenerator;
import com.octo.captcha.component.image.deformation.ImageDeformation;
import com.octo.captcha.component.image.deformation.ImageDeformationByFilters;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
import com.octo.captcha.component.image.wordtoimage.DeformedComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.FileDictionary;
import com.octo.captcha.component.word.wordgenerator.DictionaryWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;

import java.awt.*;
import java.awt.image.ImageFilter;

/**
 * Captcha engine based on the default Gimpy engine.
 *
 * @see com.octo.captcha.engine.image.gimpy.DefaultGimpyEngine
 */
public class CaptchaEngine extends ListImageCaptchaEngine {

    /**
     * The code is mostly based on the DefaultGimpyEngine with some small changes
     */
    @Override
    protected void buildInitialFactories() {
        ImageDeformation backDef = new ImageDeformationByFilters(new ImageFilter[]{});
        ImageDeformation textDef = new ImageDeformationByFilters(new ImageFilter[]{});
        ImageDeformation postDef = new ImageDeformationByFilters(new ImageFilter[]{});

        WordGenerator dictionaryWords = new DictionaryWordGenerator(new FileDictionary("toddlist"));
        TextPaster randomPaster = new DecoratedRandomTextPaster(
                6, 7,
                new SingleColorGenerator(Color.black),
                new TextDecorator[]{}
        );
        BackgroundGenerator back = new UniColorBackgroundGenerator(200, 100, Color.white);
        FontGenerator shearedFont = new RandomFontGenerator(30, 35);

        WordToImage word2image = new DeformedComposedWordToImage(
                shearedFont, back, randomPaster,
                backDef, textDef, postDef
        );

        this.addFactory(new GimpyFactory(dictionaryWords, word2image));
    }
}
