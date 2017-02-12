package de.m3y3r.pushtohugo.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.RevBlob;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by thomas on 12.02.2017.
 */

public class GitUtil {
    public void createRepoAndAddPost(String title, String link, Map<String, String> extras) {

        Date currentDate = new Date();
        CredentialsProvider credProv = new UsernamePasswordCredentialsProvider(getRemoteRepoCredUser(), getRemoteRepoCredPassword());

        try {
            DfsRepositoryDescription repoDesc = new DfsRepositoryDescription("pushToHugo");
            InMemoryRepository repo = new InMemoryRepository(repoDesc);
            repo.create();

            Git git = new Git(repo);
            RemoteAddCommand rac = git.remoteAdd();
            rac.setName("origin");
            rac.setUri(getRemoteUrl());
            rac.call();

            Map<String, org.eclipse.jgit.lib.Ref> remoteRefs = git.lsRemote().setCredentialsProvider(credProv).callAsMap();
            org.eclipse.jgit.lib.Ref remoteHead = remoteRefs.get(Constants.HEAD);

			/* sadly there is no way to fetch single object from remote,
			 * we actually need only a few object:
			 * - the tree object of origin/master
			 * - the tree object of content, content/post/
			 * then we could add a new blob to tree object /content/post
			 * and create a new tree object and commit.
			 */
//			git.fetch()
//				.setCredentialsProvider(credProv)
//				.setRefSpecs(new RefSpec("refs/heads/master")).call();

            DfsObjDatabase odb = repo.getObjectDatabase();

            ObjectId blobId;
            {
                ObjectInserter ins = odb.newInserter();
                // add file
                byte[] postData = createBlogPost(title, link, extras, currentDate).getBytes("UTF-8");
                blobId = ins.insert(Constants.OBJ_BLOB, postData);
                ins.flush();
                ins.close();
            }
            RevWalk walker = new RevWalk(repo);
            RevBlob rbid = walker.lookupBlob(blobId);
//			walker.parseCommit()
            walker.close();

            // add tree
            TreeFormatter tf = new TreeFormatter();
            String filename = getBlogPostFileName(currentDate, title, getBlogPostFileExtenstion());
            tf.append(filename, rbid);

            ObjectId treeId;
            {
                ObjectInserter ins = odb.newInserter();
                treeId = tf.insertTo(ins);
                ins.flush();
                ins.close();
            }

            // commit
            CommitBuilder cb = new CommitBuilder();
            cb.setTreeId(treeId);
            PersonIdent personIdent = new PersonIdent(getCommitAuthor(), getCommitEmail());
            cb.setAuthor(personIdent);
            cb.setCommitter(personIdent);
            String commitMessage =
                    "New blog post from App\n";

            cb.setMessage(commitMessage);
            byte[] commitData = cb.build();

            ObjectId commitId;
            {
                ObjectInserter ins = odb.newInserter();
                commitId = ins.insert(Constants.OBJ_COMMIT, commitData);
                ins.flush();
                ins.close();
            }
			/* create a new branch with one file for now,
			 * with this dirty trick we at least conserve the data in the remote repository
			 */
            String targetRef = "refs/heads/posts/" + filename;
            git.push()
                    .setCredentialsProvider(credProv)
                    .add("" + commitId.name() + ":" + targetRef).call();

            git.close();
            repo.close();
        } catch (IOException | GitAPIException | URISyntaxException e) {
            e.printStackTrace();
        } finally {}
    }

    /**
     * create a blog post from a template file
     * @param title
     * @param extras
     * @param url
     * @return
     */
    private String createBlogPost(String title, String url, Map<String,String> extras, Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        // create meta data
        StringBuffer sb = new StringBuffer();
        sb
                .append("---\n")
                .append("title: ").append(title).append("\n")
                .append("type: post\n")
                .append("date: ").append(sdf.format(date)).append("\n");
        for(Map.Entry<String, String> e : extras.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        sb.append("\n")
                .append("---\n")
                .append("<" + url + ">");

        return sb.toString();
    }

    private String getBlogPostFileExtenstion() {
        return ".md";
    }

    private String getBlogPostFileName(Date date, String title, String fileExtension) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        for(int i=0, n=title.length(); i < n; i++) {
            char c = title.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_') {
                sb.append(c);
            } else if(c >= 'A' && c <= 'Z') {
                sb.append(Character.toLowerCase(c));
            } else if(c == ' ') {
                sb.append('-');
            }
        }
        return "" + sdf.format(date) + '-' + sb.toString() + fileExtension;
    }

    /*** configuration - this should be put in some config activity or something ***/
    private String getCommitEmail() {
        return "youremail@example.com";
    }

    private String getCommitAuthor() {
        return "Your Name";
    }

    private URIish getRemoteUrl() throws URISyntaxException {
        return new URIish("http://git.example.com/yourepo");
    }

    private String getRemoteRepoCredUser() {
        return "username";
    }

    private String getRemoteRepoCredPassword() {
        return "password";
    }
}
