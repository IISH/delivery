package org.socialhistoryservices.delivery.util.gimpy;

import com.octo.captcha.CaptchaException;
import com.octo.captcha.CaptchaQuestionHelper;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

public class GimpyFactory extends ImageCaptchaFactory {
    private final Random myRandom = new SecureRandom();
    private final WordToImage wordToImage;
    private final WordGenerator wordGenerator;

    public static final String BUNDLE_QUESTION_KEY = "com.octo.captcha.image.gimpy.Gimpy";

    public GimpyFactory(WordGenerator generator, WordToImage word2image) {
        if (word2image == null) {
            throw new CaptchaException("Invalid configuration for a GimpyFactory : WordToImage can't be null");
        } else if (generator == null) {
            throw new CaptchaException("Invalid configuration for a GimpyFactory : WordGenerator can't be null");
        } else {
            this.wordToImage = word2image;
            this.wordGenerator = generator;
        }
    }

    public ImageCaptcha getImageCaptcha() {
        return this.getImageCaptcha(Locale.getDefault());
    }

    public WordToImage getWordToImage() {
        return this.wordToImage;
    }

    public WordGenerator getWordGenerator() {
        return this.wordGenerator;
    }

    public ImageCaptcha getImageCaptcha(Locale locale) {
        Integer wordLength = this.getRandomLength();
        String word = this.getWordGenerator().getWord(wordLength, locale);
        BufferedImage image;

        try {
            image = this.getWordToImage().getImage(word);
        } catch (Exception ex) {
            throw new CaptchaException(ex);
        }

        return new Gimpy(CaptchaQuestionHelper.getQuestion(locale, BUNDLE_QUESTION_KEY), image, word);
    }

    protected Integer getRandomLength() {
        int range = this.getWordToImage().getMaxAcceptedWordLength() - this.getWordToImage().getMinAcceptedWordLength();
        int randomRange = range != 0 ? this.myRandom.nextInt(range + 1) : 0;
        return randomRange + this.getWordToImage().getMinAcceptedWordLength();
    }
}