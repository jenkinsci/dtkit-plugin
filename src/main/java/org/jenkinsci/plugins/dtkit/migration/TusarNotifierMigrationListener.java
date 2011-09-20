package org.jenkinsci.plugins.dtkit.migration;

import com.thalesgroup.hudson.plugins.tusarnotifier.TusarNotifier;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.TopLevelItem;
import hudson.model.listeners.ItemListener;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import org.jenkinsci.plugins.dtkit.DTKitBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gregory Boissinot
 */
@Extension
public class TusarNotifierMigrationListener extends ItemListener {

    private static Logger LOGGER = Logger.getLogger(TusarNotifierMigrationListener.class.getName());

    @Override
    public void onLoaded() {
        List<TopLevelItem> items = Hudson.getInstance().getItems();
        for (TopLevelItem item : items) {
            try {
                if (item instanceof Project) {
                    Project project = (Project) item;
                    DescribableList<Publisher, Descriptor<Publisher>> publisherDescriptorDescribableList = project.getPublishersList();

                    List<Builder> builders = project.getBuilders();

                    Iterator<Publisher> publisherIterator = publisherDescriptorDescribableList.iterator();
                    while (publisherIterator.hasNext()) {
                        Publisher publisher = publisherIterator.next();
                        if (publisher instanceof TusarNotifier) {
                            TusarNotifier tusarNotifier = (TusarNotifier) publisher;
                            DTKitBuilder dtKitBuilder = new DTKitBuilder(
                                    tusarNotifier.getTests(),
                                    tusarNotifier.getCoverages(),
                                    tusarNotifier.getViolations(),
                                    tusarNotifier.getMeasures());

                            DescribableList<Builder, Descriptor<Builder>> newBuilders
                                    = new DescribableList<Builder, Descriptor<Builder>>(item, builders);

                            Field buildersField = Project.class.getDeclaredField("builders");
                            buildersField.setAccessible(true);

                            //Add new dtkit builder
                            newBuilders.add(dtKitBuilder);
                            buildersField.set(project, newBuilders);

                            //Remove old tusarnotifier
                            publisherIterator.remove();

                            //Save job config
                            item.save();
                        }
                    }
                }

            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Can't migrate old plugins to EnvInject plugin for the item %s", item.getName());
                ioe.printStackTrace();
            } catch (IllegalAccessException ie) {
                LOGGER.log(Level.SEVERE, "Can't migrate old plugins to EnvInject plugin for the item %s", item.getName());
                ie.printStackTrace();
            } catch (NoSuchFieldException nse) {
                LOGGER.log(Level.SEVERE, "Can't migrate old plugins to EnvInject plugin for the item %s", item.getName());
                nse.printStackTrace();
            }

        }
    }
}