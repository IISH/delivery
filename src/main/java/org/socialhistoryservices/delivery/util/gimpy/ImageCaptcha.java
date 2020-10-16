package org.socialhistoryservices.delivery.util.gimpy;

import com.octo.captcha.Captcha;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class ImageCaptcha implements Captcha {
    private boolean hasChallengeBeenCalled;
    protected String question;
    protected transient BufferedImage challenge;

    protected ImageCaptcha(String question, BufferedImage challenge) {
        this.hasChallengeBeenCalled = false;
        this.challenge = challenge;
        this.question = question;
    }

    public final String getQuestion() {
        return this.question;
    }

    public final Object getChallenge() {
        return this.getImageChallenge();
    }

    public final BufferedImage getImageChallenge() {
        this.hasChallengeBeenCalled = true;
        return this.challenge;
    }

    public final void disposeChallenge() {
        this.challenge = null;
    }

    public Boolean hasGetChalengeBeenCalled() {
        return this.hasChallengeBeenCalled;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        if (this.challenge != null) {
            ImageIO.write(this.challenge, "jpeg", out);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        try {
            this.challenge = ImageIO.read(in);
        } catch (IOException ex) {
            if (!this.hasChallengeBeenCalled) {
                throw ex;
            }
        }
    }
}
